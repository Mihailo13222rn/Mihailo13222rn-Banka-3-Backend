package rs.raf.user_service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor


public class CreateClientDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String phone;
    private String gender;
    private Date birthDate;
}
