package com.example.logmanager.controller;

import com.example.logmanager.model.LogLevel;
import com.example.logmanager.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
public class LogController {

    private final LogService service;

    @Autowired
    public LogController(LogService service) {
        this.service = service;
    }

    @GetMapping("/")
    public ModelAndView index(@RequestParam(required = false) LogLevel level) {
        ModelAndView modelAndView = new ModelAndView("index");

        modelAndView.addObject("logs", service.find(level));
        modelAndView.addObject("stats", service.stats());
        modelAndView.addObject("levels", LogLevel.values());
        modelAndView.addObject("selected", level);

        return modelAndView;
    }

    @PostMapping("/logs")
    public String add(@RequestParam LogLevel level,
                      @RequestParam String message,
                      RedirectAttributes ra) {
        if (message.isBlank()) {
            ra.addFlashAttribute("error", "Съобщението не може да е празно.");
        } else {
            service.add(level, message);
            ra.addFlashAttribute("info", "Записът е добавен.");
        }
        return "redirect:/";
    }

    @PostMapping("/logs/import")
    public String importFile(@RequestParam("file") MultipartFile file,
                             RedirectAttributes ra) {
        if (file.isEmpty()) {
            ra.addFlashAttribute("error", "Изберете файл.");
            return "redirect:/";
        }
        try {
            int n = service.importFile(file);
            ra.addFlashAttribute("info", "Импортирани редове: " + n);
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Файлът не може да бъде прочетен.");
        }
        return "redirect:/";
    }
}