package com.fileSharing.Controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fileSharing.service.FileService;

@Controller
@RequestMapping("/files")
public class fileController {

    @Autowired
    private FileService fileService;

    @GetMapping("/home")
    public String login() {
        return "home";
    }

    @GetMapping("/list")
    public String files(Model model) {
        model.addAttribute("files", fileService.getAll());
        return "list-files";
    }

    @PostMapping("/upload")
    public String postMethodName(@RequestParam("file") MultipartFile file,
            @RequestParam("uploadedBy") String uploadedby) throws IOException {
        fileService.uploadFile(file, uploadedby);
        return "redirect:/files/list";
    }

    @GetMapping("/share/{id}")
    public String shareFile(@PathVariable int id, Model model) {
        ResponseEntity<?> fileModel = fileService.shareFile(id);
        if (fileModel.hasBody()) {
            String currentUrl = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();
            model.addAttribute("shareUrl", currentUrl);
            model.addAttribute("file", fileModel.getBody());
            return "share-files";
        } else {
            return "redirect:/files/list";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteFile(@PathVariable int id) {
        ResponseEntity<?> file = fileService.deleteFile(id);
        if (file.hasBody()) {
            return "redirect:/files/list";
        } else {
            return "redirect:/files/home";
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadFile(@PathVariable("id") int id) {
        return fileService.getFile(id);
    }

    @GetMapping("/share")
    public String share() {
        return "share-files";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

}
