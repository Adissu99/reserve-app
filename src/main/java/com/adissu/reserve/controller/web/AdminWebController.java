package com.adissu.reserve.controller.web;

import com.adissu.reserve.repository.AdminConfigRepository;
import com.adissu.reserve.repository.ClientRepository;
import com.adissu.reserve.repository.ProductRepository;
import com.adissu.reserve.service.AdminConfigService;
import com.adissu.reserve.service.ProductService;
import com.adissu.reserve.util.WebUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.security.RolesAllowed;
import java.util.ArrayList;
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

    @GetMapping("/admin-index")
    @RolesAllowed("admin")
    public String showAdminIndex() {
        return "/admin/index";
    }

    @GetMapping("/admin/users")
    @RolesAllowed("admin")
    public String showUsers(Model model) {
        model.addAttribute("users", clientRepository.findAll());
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
        boolean result = productService.modifyExistingProduct(id, duration, name);
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("availableDurations", availableDurations);
        model.addAttribute("result", String.valueOf(result));

        return "/admin/products";
    }

    @PostMapping("/admin/handle-product-insert")
    @RolesAllowed("admin")
    public String handleProductInsert(Model model, @RequestParam("product-name-insert") String name, @RequestParam("product-duration-insert") String duration) {
        log.info("handleProductInsert - Got name: {}; duration: {}; from input", name, duration);

        List<String> availableDurations = WebUtil.getAvailableDurationsForProducts();
        boolean result = productService.insertNewProduct(name, duration);
        model.addAttribute("availableDurations", availableDurations);
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("result", String.valueOf(result));

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

}
