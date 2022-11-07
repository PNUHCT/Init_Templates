package TemplateProject.Template.domain.account.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Setter @Getter @NoArgsConstructor
@Entity
public class Account {
    @Column(name = "account_id") @Id
    private long id;

    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String nickname;

    private String role;

    private String profile;

    public Account (long id) { this.id = id; }

}
