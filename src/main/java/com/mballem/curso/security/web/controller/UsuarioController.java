package com.mballem.curso.security.web.controller;

import com.mballem.curso.security.datatables.Datatables;
import com.mballem.curso.security.domain.Perfil;
import com.mballem.curso.security.domain.PerfilTipo;
import com.mballem.curso.security.domain.Usuario;
import com.mballem.curso.security.repository.EspecialidadeRepository;
import com.mballem.curso.security.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("u")
public class UsuarioController {

    private UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/novo/cadastro/usuario")
    public String cadastroPorAdminParaAdminMedicoPaciente(Usuario usuario) {
        return "usuario/cadastro";
    }

    //abrir lista de usuários
    @GetMapping("/lista")
    public String listarUsuarios() {
        return "usuario/lista";
    }

    //listar usuários na datatable
    @GetMapping("/datatables/server/usuarios")
    public ResponseEntity<?> listarUsuariosDatatables(HttpServletRequest request) {
        return ResponseEntity.ok(usuarioService.buscarTodos(request));
    }

    //cadastro de usuario pelo administrador
    @PostMapping("/cadastro/salvar")
    public String salvarUsuario(Usuario usuario, RedirectAttributes attr) {
        List<Perfil> perfis = usuario.getPerfis();
        if (perfis.size() > 2 ||
                perfis.containsAll(Arrays.asList(new Perfil(1L), new Perfil(3L))) ||
                perfis.containsAll(Arrays.asList(new Perfil(2L), new Perfil(3L)))) {
            attr.addFlashAttribute("falha", "Paciente não pode ser ADMIN e/ou MÉDICO");
            attr.addFlashAttribute("usuario", usuario);
        } else {
            try {
                usuarioService.salvarUsuario(usuario);
                attr.addFlashAttribute("sucesso", "Operação realizada com sucesso");

            } catch (DataIntegrityViolationException ex) {
                String email = usuario.getEmail();
                attr.addFlashAttribute("falha", "Cadastro não realizado, email já existente:  " + email);
            }
        }
        return "redirect:/u/novo/cadastro/usuario";
    }

    //este metodo pega o id do usuario e pesquisa no banco de dados, pega as informações dele e retorna
    // para a pagina de cadastro preenchendo os campos com as informações do usuario
    @GetMapping("/editar/credenciais/usuario/{id}")
    public ModelAndView preEditarCredenciais(@PathVariable("id") Long id) {

        return new ModelAndView("usuario/cadastro", "usuario", usuarioService.buscarPorId(id));
    }

    @GetMapping("/editar/dados/usuario/{id}/perfis/{perfis}")
    public ModelAndView preEditarCadastroDadosPessoais(@PathVariable("id") Long usuarioId,
                                                       @PathVariable("perfis") Long[] perfisId) {
        Usuario usuario = usuarioService.buscarPorIdEPerfis(usuarioId, perfisId);

        if (usuario.getPerfis().contains(new Perfil(PerfilTipo.ADMIN.getCod())) &&
                !usuario.getPerfis().contains(new Perfil(PerfilTipo.MEDICO.getCod()))) {
            return new ModelAndView("usuario/cadastro", "usuario", usuario);
        } else if(usuario.getPerfis().contains(new Perfil(PerfilTipo.ADMIN.getCod())) &&
                usuario.getPerfis().contains(new Perfil(PerfilTipo.MEDICO.getCod()))) {
            return new ModelAndView("especialidade/especialidade");
        } else if(usuario.getPerfis().contains(new Perfil(PerfilTipo.PACIENTE.getCod()))) {
            ModelAndView model = new ModelAndView("error");
            model.addObject("status", 403);
            model.addObject("error", "Área restrita");
            model.addObject("message", "Os dados do paciente é restrito somente a ele..");
            return model;
        }
            return new ModelAndView("redirect:/u/lista");

    }
}