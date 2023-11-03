package culinart.service.funcionario;

import culinart.domain.fornecedor.Funcionario;
import culinart.domain.fornecedor.dto.FuncionarioCriacaoDTO;
import culinart.domain.fornecedor.dto.FuncionarioDTO;
import culinart.domain.fornecedor.dto.FuncionarioExibicaoDTO;
import culinart.domain.fornecedor.mapper.FuncionarioMapper;
import culinart.domain.fornecedor.repository.FuncionarioRepository;
import culinart.utils.ListaObj;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class FuncionarioService {
    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;


    public List<FuncionarioExibicaoDTO> listarFunc() {
        List<Funcionario> funcionarios = this.funcionarioRepository.findAll();
        return funcionarios.stream()
                .map(FuncionarioMapper::of)
                .collect(Collectors.toList());
    }

    public FuncionarioExibicaoDTO cadastrarFuncionario(FuncionarioCriacaoDTO func) {
        if (buscarFuncionarioPorBuscaBinaria(func.getEmail())) {
            throw new IllegalArgumentException("Usuario já cadastrado");
        }
        Funcionario novoFunc = FuncionarioMapper.of(func);
        String senhaCriptografada = passwordEncoder.encode(novoFunc.getSenha());
        novoFunc.setSenha(senhaCriptografada);
        return FuncionarioMapper.of(funcionarioRepository.save(novoFunc));
    }

    public FuncionarioExibicaoDTO atualizarFuncionario(Integer id, Funcionario funcionario) {
        Optional<Funcionario> func = funcionarioRepository.findById(id);
        if (func.isPresent()) {
            Funcionario funcionarioExistente = func.get();
            funcionarioExistente.setNome(funcionario.getNome());
            funcionarioExistente.setEmail(funcionario.getEmail());
            funcionarioExistente.setSenha(passwordEncoder.encode(funcionario.getSenha()));
            funcionarioExistente.setPermissao(funcionario.getPermissao());
            funcionarioExistente.setTel(funcionario.getTel());
            funcionarioExistente.setCargo(funcionario.getCargo());
            funcionarioExistente.setTurno(funcionario.getTurno());
            funcionarioExistente.setIsAtivo(funcionario.getIsAtivo());
            Funcionario funcionarioAtualizado = funcionarioRepository.save(funcionarioExistente);

            return FuncionarioMapper.of(funcionarioAtualizado);
        } else {
            System.out.println("Funcionário não encontrado");
            return null;
        }
    }

    public String deletarFuncionario(Integer id) {
        this.funcionarioRepository.deleteById(id);
        return "Deletado com sucesso";
    }

    public Boolean buscarFuncionarioPorBuscaBinaria(String email) {
        List<Funcionario> vetor = funcionarioRepository.findAll();
        for (int i = 0; i < vetor.toArray().length; i++) {
            if (vetor.get(i).getEmail().equals(email)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Boolean funcionarioIsEmpty(Integer id) {
        Optional<Funcionario> funcionario = funcionarioRepository.findById(id);
        return funcionario.isEmpty();
    }


    public List<FuncionarioDTO> funcionariosOrdenadosDTO() {
        List<FuncionarioDTO> funcionariosDTO = buscarFuncionariosDTO();
        ListaObj<FuncionarioDTO> vetorFuncionarios = new ListaObj<>(funcionariosDTO.size());

        for (FuncionarioDTO funcionarioDTO : funcionariosDTO) {
            vetorFuncionarios.adiciona(funcionarioDTO);
        }

        ordenarVetor(vetorFuncionarios);

        return vetorFuncionarios.toList();
    }

    private void ordenarVetor(ListaObj<FuncionarioDTO> vetorFuncionarios) {
        for (int i = 0; i < vetorFuncionarios.getTamanho() - 1; i++) {
            int indiceMenor = i;
            for (int j = i + 1; j < vetorFuncionarios.getTamanho(); j++) {
                if (vetorFuncionarios.getElemento(j).getNome().compareTo(vetorFuncionarios.getElemento(indiceMenor).getNome()) < 0) {
                    indiceMenor = j;
                }
            }
            FuncionarioDTO aux = vetorFuncionarios.getElemento(i);
            vetorFuncionarios.setElemento(i, vetorFuncionarios.getElemento(indiceMenor));
            vetorFuncionarios.setElemento(indiceMenor, aux);
        }
    }

    public List<FuncionarioDTO> buscarFuncionariosDTO() {
        List<Funcionario> funcionarios = funcionarioRepository.findAll();

        List<FuncionarioDTO> funcionarioDTOs = funcionarios.stream()
                .map(funcionario -> {
                    FuncionarioDTO dto = new FuncionarioDTO();
                    dto.setNome(funcionario.getNome());
                    dto.setEmail(funcionario.getEmail());
                    return dto;
                })
                .collect(Collectors.toList());

        return funcionarioDTOs;
    }

    public String gerarArquivoCsv(List<FuncionarioDTO> listaOrdenadaFunc) {
        String nomeArquivo = "funcionarios.csv";
        gravaArquivoCsv(listaOrdenadaFunc, nomeArquivo);
        leExibeArquivoCsv(nomeArquivo);
        return nomeArquivo;
    }


    public void gravaArquivoCsv(List<FuncionarioDTO> lista, String nomeArq) {
        FileWriter arq = null;
        Formatter saida = null;
        Boolean deuRuim = false;

        try {
            arq = new FileWriter(nomeArq);
            saida = new Formatter(arq);
        }
        catch (IOException erro) {
            System.out.println("Erro ao abrir o arquivo");
            System.exit(1);
        }

        try {
            saida.format("%s;%s\n","Nome", "Email");
            for (int i = 0; i < lista.size(); i++) {
                FuncionarioDTO func = lista.get(i);
                saida.format("%s;%s\n",func.getNome(), func.getEmail());
            }
        }
        catch (FormatterClosedException erro) {
            System.out.println("Erro ao gravar o arquivo");
            erro.printStackTrace();
            deuRuim = true;
        }
        finally {
            saida.close();
            try {
                arq.close();
            }
            catch (IOException erro) {
                System.out.println("Erro ao fechar o arquivo");
                deuRuim = true;
            }
            if (deuRuim) {
                System.exit(1);

            }
        }

    }

    public void leExibeArquivoCsv(String nomeArq) {
        FileReader arq = null;
        Scanner entrada = null;
        Boolean deuRuim = false;

        try {
            arq = new FileReader(nomeArq);
            entrada = new Scanner(arq).useDelimiter(";|\\n");
        }
        catch (FileNotFoundException erro) {
            System.out.println("Arquivo não encontrado!");
            System.exit(1);
        }

        try {
            while (entrada.hasNext()) {


                String nome = entrada.next();
                String email = entrada.next();


                System.out.printf("%-15s %-9s\n",nome, email);
            }
        }
        catch (NoSuchElementException erro) {
            System.out.println("Arquivo com problemas");
            erro.printStackTrace();
            deuRuim = true;
        }
        catch (IllegalStateException erro) {
            System.out.println("Erro na leitura do arquivo");
            erro.printStackTrace();
            deuRuim = true;
        }
        finally {
            entrada.close();
            try {
                arq.close();
            }
            catch (IOException erro) {
                System.out.println("Erro ao fechar o arquivo");
                deuRuim = true;
            }
            if (deuRuim) {
                System.exit(1);
            }
        }


    }


    public List<FuncionarioExibicaoDTO> leArquivoTxt(MultipartFile file){
        BufferedReader entrada = null;
        String registro, tipoRegistro;
        String email, nome, cpf, tel, rg, cargo, turno;
        LocalDate dataContratacao, dataNascimento;
        Integer permissao;
        int contaRegDadosLidos = 0;
        int qtdRegDadosGravados;
        List<FuncionarioExibicaoDTO> listaFuncsAdicionados = new ArrayList<>();
        try {
            entrada = new BufferedReader(new InputStreamReader(file.getInputStream()));
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo " + e);
        }

        try {
            registro = entrada.readLine();
            while (registro != null){
                tipoRegistro = registro.substring(0,2);
                if (tipoRegistro.equals("00")){
                    System.out.println("É um registro de header ");
                    System.out.println("Tipo de arquivo " + registro.substring(2,4));
                    System.out.println("Data e hora de geração do arquivo " + registro.substring(4,14));
                    System.out.println("Versão do documento " + registro.substring(14,16));
                } else if (tipoRegistro.equals("01")) {
                    System.out.println("É um registro de trailer");
                    qtdRegDadosGravados= Integer.parseInt(registro.substring(2, 4));
                    if (contaRegDadosLidos == qtdRegDadosGravados){
                        System.out.println("Quantidade de registros de dados gravados é compatível com a " +
                                "quantidade de registros de dados lidos");
                    }else {
                        System.out.println("Quantidade de registros de dados gravados é incompatível com a " +
                                "quantidade de registros de dados lidos");
                    }
                }else if (tipoRegistro.equals("02")) {
                    System.out.println("É um registro de corpo");
                    System.out.println("É um registro de corpo");
                    System.out.println("É um registro de corpo");
                    nome = registro.substring(2, 46).trim();
                    System.out.println("Nome: " + registro.substring(2, 46));
                    email = registro.substring(46, 91).trim();
                    System.out.println("Email: " + registro.substring(46, 91));
                    dataContratacao = LocalDate.parse(registro.substring(91, 101).trim());
                    System.out.println("Data de Contratação: " + registro.substring(91, 101));
                    cpf = registro.substring(101, 112).trim();
                    System.out.println("CPF: " + registro.substring(101, 112));
                    tel = registro.substring(112, 123).trim();
                    System.out.println("Telefone: " + registro.substring(112, 123));
                    dataNascimento = LocalDate.parse(registro.substring(123, 133).trim());
                    System.out.println("Data de Nascimento: " + registro.substring(123, 133));
                    rg = registro.substring(133, 142).trim();
                    System.out.println("RG: " + registro.substring(133, 142));
                    cargo = registro.substring(142, 162).trim();
                    System.out.println("Cargo: " + registro.substring(142, 162));
                    turno = registro.substring(162, 172).trim();
                    System.out.println("Turno: " + registro.substring(162, 172));
                    permissao = Integer.valueOf(registro.substring(172, 173).trim());
                    System.out.println("Permissão: " + registro.substring(172, 173));


                    contaRegDadosLidos++;
                    FuncionarioCriacaoDTO funcCriacao = new FuncionarioCriacaoDTO();
                    funcCriacao.setNome(nome);
                    funcCriacao.setEmail(email);
                    funcCriacao.setDataContratacao(dataContratacao);
                    funcCriacao.setCpf(cpf);
                    funcCriacao.setTel(tel);
                    funcCriacao.setDataNascimento(dataNascimento);
                    funcCriacao.setRg(rg);
                    funcCriacao.setCargo(cargo);
                    funcCriacao.setTurno(turno);
                    funcCriacao.setPermissao(permissao);
                    listaFuncsAdicionados.add(cadastrarFuncionario(funcCriacao));
                }else {
                    System.out.println("Registro inválido");
                }
                registro = entrada.readLine();
            }
            entrada.close();
        }catch (IOException err){
            System.out.println("Erro ao ler os registros" + err);
        }
        return listaFuncsAdicionados;
    }
}

