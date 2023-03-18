package com.poc.qrapp.QRController;


import com.poc.qrapp.QRService.QRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

@Controller
public class QRController {

    @Autowired
    private QRService qrService;

    @RequestMapping ("/")
    public String createQR (Model model, HttpServletRequest request) {
        return qrService.generateQrCode(ServletUriComponentsBuilder.fromRequest(request).replacePath(null).build().toUriString(), model);
    }

    @RequestMapping("{id}")
    public String loadLoginForm(@PathVariable("id") String refId, Model model) {
        return qrService.loadLoginForm(refId, model);
    }

    @PostMapping("{id}")
    public String login(@PathVariable("id") String refId, Model model) {
        return qrService.login(refId, model);
    }

    @GetMapping("poll")
    public ResponseEntity<?> poll(@RequestParam("id") String id) {
        return qrService.pollData(id);
    }

    @PostMapping("{id}/redirect")
    public String redirect (@PathVariable("id") String refId, Model model) {
        return qrService.redirect(refId, model);
    }

}
