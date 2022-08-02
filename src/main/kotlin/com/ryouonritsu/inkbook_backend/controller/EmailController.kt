package com.ryouonritsu.inkbook_backend.controller

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@Hidden
class EmailController {
    @RequestMapping("/change_email")
    @Hidden
    fun changeEmail(@RequestParam("email") email: String, model: Model): String {
        model.addAttribute("email", email)
        return "change_email"
    }

    @RequestMapping("/forgot_password")
    @Hidden
    fun forgotPassword(@RequestParam("verification_code") email: String, model: Model): String {
        model.addAttribute("verification_code", email)
        return "forgot_password"
    }

    @RequestMapping("/registration_verification")
    @Hidden
    fun registrationVerification(@RequestParam("verification_code") email: String, model: Model): String {
        model.addAttribute("verification_code", email)
        return "registration_verification"
    }
}