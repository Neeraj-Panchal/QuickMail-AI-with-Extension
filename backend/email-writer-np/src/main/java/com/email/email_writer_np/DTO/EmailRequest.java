package com.email.email_writer_np.DTO;

import lombok.Data;

@Data
public class EmailRequest {
    private String emailContent;
    private String tone;
}
