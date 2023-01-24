package com.adissu.reserve.controller.web;

import com.adissu.reserve.constants.ResultConstants;
import com.adissu.reserve.entity.Client;
import com.adissu.reserve.entity.InviteCode;
import com.adissu.reserve.entity.Product;
import com.adissu.reserve.entity.Reservation;
import com.adissu.reserve.service.ClientService;
import com.adissu.reserve.service.InviteCodeService;
import com.adissu.reserve.service.ReservationService;
import com.adissu.reserve.util.DateUtil;
import com.adissu.reserve.util.WebUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
@RequiredArgsConstructor
public class UserWebController {

    private final ReservationService reservationService;
    private final ClientService clientService;
    private final InviteCodeService inviteCodeService;
    private final List<Product> productList;

    @Context
    private SecurityContext sc;

    @GetMapping("/logout")
    @RolesAllowed({"client", "admin"})
    public String logout(HttpServletRequest httpServletRequest, Model model) {
        try {
            httpServletRequest.logout();

            log.info("User {} successfully logged out.", WebUtil.getUsername(httpServletRequest));
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }

        model.addAttribute("role", "unregistered");
        return "/unregistered/index";
    }

    @GetMapping("/login-form")
    @RolesAllowed({"client", "admin"})
    public String login(Model model, HttpServletRequest httpServletRequest) {
        String role = WebUtil.getRole(httpServletRequest);
        model.addAttribute("role", role);
        return "/unregistered/index";
    }

    @PostMapping(path = "/handle-reservation", params = "submitButton=time")
    @RolesAllowed({"client", "admin"})
    public String handleGetFreeTime(@RequestParam("selected-date") String selectedDate, @RequestParam("selected-product") String productName, Model model) {
        log.info("Inside handleGetFreeTime");
        List<String> freeTimeAvailable = reservationService.getFreeTimeForDate(selectedDate, productName);
        model.addAttribute("productList", productList);

        if( freeTimeAvailable.isEmpty() ) {
            model.addAttribute("result", ResultConstants.FULL_DAY);
        } else {
            model.addAttribute("freeTimeList", freeTimeAvailable);
            model.addAttribute("result", ResultConstants.AVAILABLE);
        }
        model.addAttribute("alreadySelectedDate", selectedDate);
        model.addAttribute("alreadySelectedProduct", productName);
        return "/user/reserve";
    }

    @PostMapping(path = "/handle-reservation", params = "submitButton=reserve")
    @RolesAllowed({"client", "admin"})
    public String handleReserve(@RequestParam("selected-date") String selectedDate, @RequestParam("selected-product") String productName, @RequestParam("selected-hour") String selectedHour, Model model, HttpServletRequest httpServletRequest) {
        log.info("Inside handleReserve");
        String clientName = WebUtil.getUsername(httpServletRequest);
        String date = WebUtil.formatDateString(selectedDate);
        String result = reservationService.doReserve(selectedHour, clientName, productName, selectedDate);

        log.info("Selected date from input {}", selectedDate);

        model.addAttribute("result", result);
        model.addAttribute("productList", productList);
        model.addAttribute("alreadySelectedDate", date);
        model.addAttribute("alreadySelectedProduct", productName);
        model.addAttribute("alreadySelectedHour", selectedHour);
        return "/user/made-reserve";
    }

    @GetMapping("/show-reserve")
    @RolesAllowed({"client", "admin"})
    public String showReserveForm(Model model) {
        model.addAttribute("productList", productList);
        return "/user/reserve";
    }

    @GetMapping("/after-reserve")
    @RolesAllowed({"client", "admin"})
    public String handleAfterReserve(@RequestParam("submitButton") String destination, Model model, HttpServletRequest httpServletRequest) {
        String page = "";
        switch (destination) {
            case "info":
                page = "redirect:/info-client";
                break;
            case "index":
                String role = WebUtil.getRole(httpServletRequest);
                model.addAttribute("role", role);
                page = "/unregistered/index";
                break;
            case "reserve":
                model.addAttribute("productList", productList);
                page = "/user/reserve";
                break;
            default:
                page = "/unregistered/index";
        }

        return page;
    }

    @GetMapping("/info-client")
    @RolesAllowed({"client", "admin"})
    public String handleUserInfo(Model model, HttpServletRequest httpServletRequest) {
        String email = WebUtil.getUsername(httpServletRequest);
        List<Client> invitedUsersList = clientService.getInvitedUsersList(email);

        model.addAttribute("invitedUsersList", invitedUsersList);
        model.addAttribute("invitedUsersActivatedCheck", clientService.getClientsActivatedFromList(invitedUsersList));
        model.addAttribute("client", clientService.getClient(email));
        model.addAttribute("reservationList", clientService.getReservationsMade(email));
        model.addAttribute("cancelledReservationList", clientService.getCancelledReservations(email));
        model.addAttribute("tomorrowDate", DateUtil.getDateWithoutTimeFromToday(1));
        model.addAttribute("yesterdayDate", DateUtil.getDateWithoutTimeFromToday(-1));

        for(Reservation reservation : clientService.getReservationsMade(email)) {
            if( reservation.getSelectedDate().after(DateUtil.getDateWithoutTimeFromToday(1)) ) {
                log.info("Reservation with id {} and date {} is available for cancelling", reservation.getId(), reservation.getSelectedDate());
            } else if( reservation.getSelectedDate().after(DateUtil.getDateWithoutTimeFromToday(-1)) ) {
                log.info("Reservation with id {} and date {} is available for requesting cancelling", reservation.getId(), reservation.getSelectedDate());
            }
        }

        return "/user/info";
    }

    @GetMapping("/show-invite")
    @RolesAllowed("client")
    public String showInviteCodeForm(Model model, HttpServletRequest httpServletRequest) {
        String email = WebUtil.getUsername(httpServletRequest);
        List<InviteCode> inviteCodes = inviteCodeService.getInviteCodes(email);
        boolean letUserGenerate = inviteCodeService.letUserGenerate(inviteCodes);

        model.addAttribute("inviteCodes", inviteCodes);
        model.addAttribute("letUserGenerate", letUserGenerate);

        return "/user/invite";
    }

    @PostMapping("/handle-invite")
    @RolesAllowed("client")
    public String generateInviteCode(Model model, HttpServletRequest httpServletRequest) {
        String email = WebUtil.getUsername(httpServletRequest);
        Optional<InviteCode> inviteCode = inviteCodeService.generateInviteCode(email);
        List<InviteCode> inviteCodes = inviteCodeService.getInviteCodes(email);
        boolean letUserGenerate = inviteCodeService.letUserGenerate(inviteCodes);

        model.addAttribute("inviteCodes", inviteCodes);
        model.addAttribute("letUserGenerate", letUserGenerate);

        if( inviteCode.isEmpty() ) {
            model.addAttribute("result", ResultConstants.ERROR_NOT_FOUND);
        } else {
            model.addAttribute("result", ResultConstants.SUCCESS);
            model.addAttribute("inviteCode", inviteCode.get().getInvCode());
        }

        return "/user/invite";
    }

    @PostMapping("/delete-reservation")
    @RolesAllowed("client")
    public String cancelReservation(@RequestParam(value = "cancelButton", required = false) String cancelButtonId, @RequestParam(value = "requestCancelButton", required = false) String requestCancelButtonId) {
        log.info("Got cancelButtonId {}; requestCancelButtonId {} from input", cancelButtonId, requestCancelButtonId);
        String result;
        if( cancelButtonId == null || cancelButtonId.isBlank() ) {
            result = reservationService.requestCancelReservation(requestCancelButtonId);
            log.info("Result from requested cancelling reservation {}", result);
        } else {
            result = reservationService.cancelReservation(cancelButtonId);
            log.info("Result from cancelling reservation {}", result);
        }

        return "redirect:/info-client";
    }

}
