package com.adissu.reserve.controller.web;

import com.adissu.reserve.entity.Client;
import com.adissu.reserve.entity.InviteCode;
import com.adissu.reserve.entity.Product;
import com.adissu.reserve.service.ClientService;
import com.adissu.reserve.service.InviteCodeService;
import com.adissu.reserve.service.ProductService;
import com.adissu.reserve.service.ReservationService;
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
    private final ProductService productService;
    private final ClientService clientService;
    private final InviteCodeService inviteCodeService;
    private final List<Product> productList;

    @Context
    private SecurityContext sc;

    @GetMapping("/logout")
    @RolesAllowed({"client", "admin"})
    public String logout(HttpServletRequest httpServletRequest) {
        try {
            httpServletRequest.logout();

            log.info("User {} successfully logged out.", WebUtil.getUsername(httpServletRequest));
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }

        return "/user/logout";
    }

    @PostMapping(path = "/handle-reservation", params = "submitButton=time")
    @RolesAllowed({"client", "admin"})
    public String handleGetFreeTime(@RequestParam("selected-date") String selectedDate, @RequestParam("selected-product") String productName, Model model) {
        log.info("Inside handleGetFreeTime");
        List<String> freeTimeAvailable = reservationService.getFreeTimeForDate(selectedDate, productName);
        model.addAttribute("productList", productList);

        if( freeTimeAvailable.isEmpty() ) {
            model.addAttribute("result", "FULL_DAY");
        } else {
            model.addAttribute("freeTimeList", freeTimeAvailable);
            model.addAttribute("result", "AVAILABLE");
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
                String email = WebUtil.getUsername(httpServletRequest);
                List<Client> invitedUsersList = clientService.getInvitedUsersList(email);

                model.addAttribute("invitedUsersList", invitedUsersList);
                model.addAttribute("invitedUsersActivatedCheck", clientService.getClientsActivatedFromList(invitedUsersList));
                model.addAttribute("client", clientService.getClient(email));
                model.addAttribute("reservationList", clientService.getReservationsMade(email));
                page = "/user/info";
                break;
            case "index":
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

        return "/user/info";
    }

    @GetMapping("/show-invite")
    @RolesAllowed("client")
    public String showInviteCodeForm(Model model, HttpServletRequest httpServletRequest) {
        String email = WebUtil.getUsername(httpServletRequest);
        List<InviteCode> inviteCodes = inviteCodeService.getInviteCodes(email);

        model.addAttribute("inviteCodes", inviteCodes);

        return "/user/invite";
    }

    @PostMapping("/handle-invite")
    public String generateInviteCode(Model model, HttpServletRequest httpServletRequest) {
        String email = WebUtil.getUsername(httpServletRequest);
        Optional<InviteCode> inviteCode = inviteCodeService.generateInviteCode(email);
        List<InviteCode> inviteCodes = inviteCodeService.getInviteCodes(email);

        model.addAttribute("inviteCodes", inviteCodes);
        if( inviteCode.isEmpty() ) {
            model.addAttribute("result", "ERROR");
        } else {
            model.addAttribute("result", "SUCCESS");
            model.addAttribute("inviteCode", inviteCode.get().getInvCode());
        }

        return "/user/invite";
    }
}
