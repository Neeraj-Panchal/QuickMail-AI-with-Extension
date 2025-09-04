package com.email.email_writer_np.app;

import com.email.email_writer_np.DTO.EmailRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//"/api/email/generate"
@RestController
@RequestMapping("/api/email")
@AllArgsConstructor 
@CrossOrigin(origins = "*")
public class EmailGeneratorController {

    private final EmailGeneraterService emailGeneraterService;

    @PostMapping("/generate")
    ResponseEntity<String> generateEmail(@RequestBody EmailRequest emailRequest){
        String response = emailGeneraterService.generateEmailReply(emailRequest);
        return ResponseEntity.ok(response);
    }
}

// /api/email/generate