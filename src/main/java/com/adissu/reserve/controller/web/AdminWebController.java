package com.adissu.reserve.controller.web;

import com.adissu.reserve.constants.ResultConstants;
import com.adissu.reserve.entity.CancelledReservation;
import com.adissu.reserve.entity.Client;
import com.adissu.reserve.repository.AdminConfigRepository;
import com.adissu.reserve.repository.CancelledReservationRepository;
import com.adissu.reserve.repository.ClientRepository;
import com.adissu.reserve.repository.ProductRepository;
import com.adissu.reserve.service.AdminConfigService;
import com.adissu.reserve.service.AdminService;
import com.adissu.reserve.service.ProductService;
import com.adissu.reserve.util.WebUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.security.RolesAllowed;
import java.util.HashMap;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class AdminWebController {

    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final AdminConfigRepository adminConfigRepository;
    private final ProductService productService;
    private final AdminConfigService adminConfigService;
    private final AdminService adminService;
    private final CancelledReservationRepository cancelledReservationRepository;

    @GetMapping("/admin-index")
    @RolesAllowed("admin")
    public String showAdminIndex() {
        return "/admin/index";
    }

    @GetMapping("/admin/users")
    @RolesAllowed("admin")
    public String showUsers(Model model) {
        List<Client> allClients = clientRepository.findAll();
        List<Client> invitedClients = adminService.getInvitedUsersList();
        HashMap<Integer, Integer> cancelledReservationsMap = adminService.getCancelledReservationsUsers(allClients);
        String result = (String) model.getAttribute("result");
        List<Client> userChainList = (List<Client>) model.getAttribute("userChainList");

        log.info("Got result = {}; and List of size {}", result, userChainList != null ? userChainList.size() : "null");

        model.addAttribute("users", clientRepository.findAll());
        model.addAttribute("invitedClients", invitedClients);
        model.addAttribute("result", result);
        model.addAttribute("userChainList", userChainList);
        model.addAttribute("cancelledReservationsMap", cancelledReservationsMap);
        return "/admin/users";
    }

    @GetMapping("/admin/show-product-form")
    @RolesAllowed("admin")
    public String showProductForm(Model model) {
        List<String> availableDurations = WebUtil.getAvailableDurationsForProducts();
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("availableDurations", availableDurations);
        return "/admin/products";
    }

    @PostMapping("/admin/handle-product-modify")
    @RolesAllowed("admin")
    public String handleProductModify(Model model, @RequestParam("product-id-modify") String id, @RequestParam("product-name-modify") String name, @RequestParam("product-duration-modify") String duration) {
        log.info("handleProductModify - Got id: {}; name: {}; duration: {}; from input", id, name, duration);

        List<String> availableDurations = WebUtil.getAvailableDurationsForProducts();
        String result = productService.modifyExistingProduct(id, duration, name);
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("availableDurations", availableDurations);
        model.addAttribute("result", result);

        return "/admin/products";
    }

    @PostMapping("/admin/handle-product-insert")
    @RolesAllowed("admin")
    public String handleProductInsert(Model model, @RequestParam("product-name-insert") String name, @RequestParam("product-duration-insert") String duration) {
        log.info("handleProductInsert - Got name: {}; duration: {}; from input", name, duration);

        List<String> availableDurations = WebUtil.getAvailableDurationsForProducts();
        String result = productService.insertNewProduct(name, duration);
        model.addAttribute("availableDurations", availableDurations);
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("result", result);

        return "/admin/products";
    }

    // config form for: max reserve per client, max invite codes per client, set starting hour for day, set final hour for day, etc..
    @GetMapping("/admin/show-config-form")
    @RolesAllowed("admin")
    public String showConfigForm(Model model) {
        model.addAttribute("availableConfigs", adminConfigRepository.findAll());
        return "/admin/config";
    }

    // after a new config is inserted. you have to manually add it to AdminConfigConstants
    @PostMapping("/admin/handle-config-insert")
    @RolesAllowed("admin")
    public String handleConfigInsert(Model model, @RequestParam("config-name-insert") String name, @RequestParam("config-value-insert") String value) {
        log.info("handleConfigInsert - Got name = {}; value = {}; from input", name, value);

        String result = adminConfigService.addNewConfig(name, value);

        model.addAttribute("availableConfigs", adminConfigRepository.findAll());
        model.addAttribute("result", result);
        return "/admin/config";
    }

    @PostMapping("/admin/handle-config-modify")
    @RolesAllowed("admin")
    public String handleConfigModify(Model model, @RequestParam("config-name-modify") String name, @RequestParam("config-value-modify") String value) {
        log.info("handleConfigModify - Got name = {}; value = {}; from input", name, value);

        String result = adminConfigService.modifyValueForConfig(name, value);

        model.addAttribute("availableConfigs", adminConfigRepository.findAll());
        model.addAttribute("result", result);
        return "/admin/config";
    }

    @PostMapping("/admin/make-admin")
    @RolesAllowed("admin")
    public String makeSomeoneAdmin(@RequestParam("userIdAdmin") String id) {
        if( adminService.makeAdmin(id).contains("ERROR") ) {
            log.info("An error has occurred while trying to make user with id {} an admin.", id);
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/admin/cancel-reservation")
    @RolesAllowed("admin")
    public String approveCancelRequest(@RequestParam("cancel-id") String cancelId) {
        if( adminService.approveCancelRequest(cancelId).contains("ERROR") ) {
            log.info("An error has occurred while trying to approve the cancel request.");
        }

        return "redirect:/admin/show-cancel-requests-form";
    }

    @GetMapping("/admin/show-cancel-requests-form")
    @RolesAllowed("admin")
    public String showCancelRequestsForm(Model model) {
        List<CancelledReservation> cancelledReservationList = cancelledReservationRepository.findAllByRequestedAndDone(true, false);
        if( cancelledReservationList.size() == 0 ) {
            log.info("List is empty.");
            model.addAttribute("result", ResultConstants.ERROR_LIST_EMPTY);
        } else {
            log.info("List is not empty and has size {}", cancelledReservationList.size());
            model.addAttribute("result", ResultConstants.SUCCESS_LIST_NOT_EMPTY);
            model.addAttribute("cancelledReservationsList", cancelledReservationList);
        }
        return "/admin/cancel-requests";
    }

    @GetMapping("/admin/user-chain")
    @RolesAllowed("admin")
    public String getUserChain(RedirectAttributes redirectAttributes, @RequestParam("selected-user-id") String selectedUserId) {
        List<Client> userChainList = adminService.getClientInvitedChain(selectedUserId);
        String result = userChainList.isEmpty() ? ResultConstants.ERROR_NOT_FOUND : ResultConstants.SUCCESS_CHAIN;

        redirectAttributes.addFlashAttribute("userChainList", userChainList);
        redirectAttributes.addFlashAttribute("result", result);

        return "redirect:/admin/users";
    }

}
