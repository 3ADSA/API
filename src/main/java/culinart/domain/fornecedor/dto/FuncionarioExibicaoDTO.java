package culinart.domain.fornecedor.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class FuncionarioExibicaoDTO {
        private Integer id;
        private String nome;
        private String email;
        private String cpf;
        private String tel;
        private String cargo;
        private String turno;
        private Integer permissao;
}
