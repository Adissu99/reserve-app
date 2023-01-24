package com.adissu.reserve.controller.web;

import com.adissu.reserve.constants.ResultConstants;
import com.adissu.reserve.dto.ClientDTO;
import com.adissu.reserve.service.ClientService;
import com.adissu.reserve.util.WebUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
@Slf4j
@RequiredArgsConstructor
public class UnregisteredWebController {

    private final ClientService clientService;

    @GetMapping("/")
    public String index(Model model, HttpServletRequest httpServletRequest) {
        String role = WebUtil.getRole(httpServletRequest);
        model.addAttribute("role", role);
        return "/unregistered/index";
    }

    @GetMapping("/static")
    public String indexStatic() {
        return "redirect:/";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("newClient", new ClientDTO());
        return "/unregistered/register";
    }

    @PostMapping("/register-client")
    public String registerClient(@Valid ClientDTO clientDTO) {
        /*if( result.hasErrors() ) {
            return "/unregistered/register";
        }*/
        log.info("Got client {} from request", clientDTO);

        clientService.registerClient(clientDTO);

        return "redirect:/";
    }

    @GetMapping("/api/client/activate")
    public String verifyEmail(@RequestParam String email, @RequestParam String code, Model model) {
        String result = clientService.activateUser(email, code);
        model.addAttribute("result", result);
        return "/unregistered/handle-verification";
    }

    // actually resending the mail
    @PostMapping("/resend-mail")
    public String resendMail(@RequestParam("email") String email, Model model) {
        String result = clientService.resendMail(email);
        model.addAttribute("result", result);
        return "/unregistered/handle-verification";
    }

    // show resend mail form
    @GetMapping("/resend-mail")
    public String resendMail(Model model) {
        model.addAttribute("result", ResultConstants.RESEND_FORM);

        return "/unregistered/handle-verification";
    }
}
