package dev.codingsales.Captive.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@Entity
@Table(name ="guest_users")
@NoArgsConstructor
public class GuestUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "nome completo e obrigatorio")
    @Size(min= 3, max = 100, message = "Nome completo deve ter entre 3 e 100 caracteres")
    @Column(nullable = false)
    @NotNull @NotBlank @NotEmpty
    private String fullName;

    @NotBlank(message = "CPF é obrigatório")
    @Size(min = 10, max = 20, message = "Numero de telefone invalido")
    @Column(nullable = false, unique = true)
    @NotNull @NotBlank @NotEmpty
    private String cpf;

    @NotBlank(message = "Email e obrigatorio")
    @Email(message ="Formato de email inválido")
    @Size(max = 100)
    @Column(name="email")
    @NotNull
    @NotBlank @Email
    @Size(max = 100)
    private String email;

    @Column(nullable = false, name="phone_number")
    private String phoneNumber;

    @Column(name="accepted_tou")
    private Boolean acceptedTou = Boolean.FALSE;
}
