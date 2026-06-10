package view;

import controller.MindDebugController;
import model.Aula;
import model.Chat;
import model.Curso;
import model.Estudante;
import model.Materia;
import model.Material;
import model.Mensagem;
import model.Monitor;
import model.Periodo;
import model.Professor;
import model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class JanelaMindDebug extends JFrame {
    private MindDebugController controller;
    private Usuario usuarioLogado;
    private Materia materiaAberta;

    public JanelaMindDebug(MindDebugController controller) {
        this.controller = controller;
        setTitle("MindDebug");
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mostrarLogin();
    }

    private void mostrarTela(JPanel painel) {
        setContentPane(painel);
        revalidate();
        repaint();
    }

    private void tentar(Runnable acao) {
        try {
            acao.run();
        } catch (RuntimeException erro) {
            JOptionPane.showMessageDialog(this, erro.getMessage(), "Atencao", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JButton botao(String texto) {
        return new JButton(texto);
    }

    private JPanel painelComMargem() {
        JPanel painel = new JPanel(new BorderLayout(12, 12));
        painel.setBorder(new EmptyBorder(16, 16, 16, 16));
        return painel;
    }

    private JPanel cabecalho(String titulo) {
        JPanel barra = new JPanel(new BorderLayout());
        JLabel nomeSistema = new JLabel("MindDebug");
        nomeSistema.setFont(new Font("Arial", Font.BOLD, 24));
        barra.add(nomeSistema, BorderLayout.WEST);

        if (usuarioLogado != null) {
            JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            acoes.add(new JLabel("Usuario: " + usuarioLogado.getNome() + " (" + usuarioLogado.getCargo() + ")"));

            JButton trocarSenha = botao("Trocar Senha");
            JButton voltar = botao("Voltar aos Cursos");
            JButton logoff = botao("Logoff");

            trocarSenha.addActionListener(e -> trocarSenha());
            voltar.addActionListener(e -> {
                materiaAberta = null;
                mostrarNavegacao();
            });
            logoff.addActionListener(e -> {
                usuarioLogado = null;
                materiaAberta = null;
                mostrarLogin();
            });

            acoes.add(trocarSenha);
            acoes.add(voltar);
            acoes.add(logoff);
            barra.add(acoes, BorderLayout.EAST);
        }

        JPanel painel = new JPanel(new BorderLayout());
        painel.add(barra, BorderLayout.NORTH);
        if (titulo != null && !titulo.isEmpty()) {
            JLabel labelTitulo = new JLabel(titulo);
            labelTitulo.setFont(new Font("Arial", Font.BOLD, 18));
            labelTitulo.setBorder(new EmptyBorder(10, 0, 0, 0));
            painel.add(labelTitulo, BorderLayout.SOUTH);
        }
        return painel;
    }

    private void mostrarLogin() {
        JPanel fundo = new JPanel(new GridBagLayout());
        JPanel caixa = new JPanel(new GridBagLayout());
        caixa.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Cadastro Login"),
                new EmptyBorder(15, 25, 15, 25)
        ));

        GridBagConstraints gbc = criarGbc();
        JLabel titulo = new JLabel("MindDebug", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        adicionarComponente(caixa, gbc, 0, titulo);

        JTextField campoUsuario = new JTextField(18);
        JPasswordField campoSenha = new JPasswordField(18);
        JButton entrar = botao("Entrar");
        JButton cadastrar = botao("Cadastrar");
        JButton esqueciSenha = botao("Esqueci minha senha");

        adicionarLinha(caixa, gbc, 1, "Usuario:", campoUsuario);
        adicionarLinha(caixa, gbc, 2, "Senha:", campoSenha);
        adicionarComponente(caixa, gbc, 3, entrar);
        adicionarComponente(caixa, gbc, 4, cadastrar);
        adicionarComponente(caixa, gbc, 5, esqueciSenha);

        JTextArea acessos = new JTextArea("Login Dev: dev / 1234\nLogin Professor: prof / 1234\nLogin Monitor: monitor / 1234\nLogin Aluno: 2026001 / 1234");
        acessos.setEditable(false);
        acessos.setOpaque(false);
        adicionarComponente(caixa, gbc, 6, acessos);

        entrar.addActionListener(e -> tentar(() -> {
            usuarioLogado = controller.fazerLogin(campoUsuario.getText(), new String(campoSenha.getPassword()));
            if (usuarioLogado.getNivel() == 4) {
                mostrarPainelDev();
            } else {
                mostrarNavegacao();
            }
        }));

        cadastrar.addActionListener(e -> abrirCadastroAluno());
        esqueciSenha.addActionListener(e -> JOptionPane.showMessageDialog(this, "Procure o Dev para redefinir sua senha."));

        fundo.add(caixa);
        mostrarTela(fundo);
    }

    private void abrirCadastroAluno() {
        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = criarGbc();

        JTextField nome = new JTextField(18);
        JTextField email = new JTextField(18);
        JTextField matricula = new JTextField(18);
        JPasswordField senha = new JPasswordField(18);
        JComboBox<Curso> curso = new JComboBox<>(controller.listarCursos().toArray(new Curso[0]));

        adicionarComponente(formulario, gbc, 0, new JLabel("MindDebug", SwingConstants.CENTER));
        adicionarLinha(formulario, gbc, 1, "Nome:", nome);
        adicionarLinha(formulario, gbc, 2, "Email:", email);
        adicionarLinha(formulario, gbc, 3, "Matricula:", matricula);
        adicionarLinha(formulario, gbc, 4, "Senha:", senha);
        adicionarLinha(formulario, gbc, 5, "Curso:", curso);

        int resposta = JOptionPane.showConfirmDialog(this, formulario, "Cadastro", JOptionPane.OK_CANCEL_OPTION);
        if (resposta == JOptionPane.OK_OPTION) {
            tentar(() -> {
                controller.cadastrarEstudante(nome.getText(), email.getText(), matricula.getText(), new String(senha.getPassword()), (Curso) curso.getSelectedItem());
                JOptionPane.showMessageDialog(this, "Cadastro realizado.");
            });
        }
    }

    private void mostrarNavegacao() {
        JPanel painel = painelComMargem();
        painel.add(cabecalho("Tela inicial"), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(10, 10));
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JComboBox<Curso> comboCurso = new JComboBox<>();
        if (usuarioLogado instanceof Estudante) {
            comboCurso.addItem(((Estudante) usuarioLogado).getCurso());
        } else {
            for (Curso curso : controller.listarCursos()) {
                comboCurso.addItem(curso);
            }
        }

        JComboBox<Periodo> comboPeriodo = new JComboBox<>();
        DefaultListModel<Materia> modeloMaterias = new DefaultListModel<>();
        JList<Materia> listaMaterias = new JList<>(modeloMaterias);
        listaMaterias.setBorder(BorderFactory.createTitledBorder("Disciplinas:"));

        Runnable atualizarPeriodos = () -> {
            comboPeriodo.removeAllItems();
            Curso curso = (Curso) comboCurso.getSelectedItem();
            if (curso != null) {
                for (Periodo periodo : curso.getPeriodos()) {
                    comboPeriodo.addItem(periodo);
                }
            }
        };

        Runnable atualizarMaterias = () -> {
            modeloMaterias.clear();
            Periodo periodo = (Periodo) comboPeriodo.getSelectedItem();
            if (periodo != null) {
                for (Materia materia : periodo.getMaterias()) {
                    modeloMaterias.addElement(materia);
                }
            }
        };

        comboCurso.addActionListener(e -> {
            atualizarPeriodos.run();
            atualizarMaterias.run();
        });
        comboPeriodo.addActionListener(e -> atualizarMaterias.run());
        atualizarPeriodos.run();
        atualizarMaterias.run();

        topo.add(new JLabel("Curso:"));
        topo.add(comboCurso);
        topo.add(new JLabel("Periodo:"));
        topo.add(comboPeriodo);

        JButton acessar = botao("Acessar Disciplina");
        JButton sair = botao("Sair");
        JPanel rodape = painelBotoes(acessar, sair);

        acessar.addActionListener(e -> tentar(() -> {
            Materia materia = listaMaterias.getSelectedValue();
            if (materia == null) {
                throw new RuntimeException("Escolha uma disciplina.");
            }
            materia.adicionarParticipante(usuarioLogado);
            materiaAberta = materia;
            mostrarMateria();
        }));

        sair.addActionListener(e -> {
            usuarioLogado = null;
            mostrarLogin();
        });

        centro.add(topo, BorderLayout.NORTH);
        centro.add(new JScrollPane(listaMaterias), BorderLayout.CENTER);
        centro.add(rodape, BorderLayout.SOUTH);
        painel.add(centro, BorderLayout.CENTER);
        mostrarTela(painel);
    }

    private void mostrarPainelDev() {
        JPanel painel = painelComMargem();
        painel.add(cabecalho(""), BorderLayout.NORTH);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Cursos e Periodos", abaCursosPeriodos());
        abas.addTab("Gestao de disciplinas", abaGestaoDisciplinas());
        abas.addTab("Gestao de Usuarios", abaGestaoUsuarios());
        painel.add(abas, BorderLayout.CENTER);

        JButton acessar = botao("Acessar Disciplina");
        JButton salvar = botao("Salvar");
        JButton sair = botao("Sair");
        JPanel rodape = painelBotoes(acessar, salvar, sair);

        acessar.addActionListener(e -> mostrarNavegacao());
        salvar.addActionListener(e -> tentar(() -> {
            controller.salvar();
            JOptionPane.showMessageDialog(this, "Dados salvos.");
        }));
        sair.addActionListener(e -> mostrarLogin());

        painel.add(rodape, BorderLayout.SOUTH);
        mostrarTela(painel);
    }

    private JPanel abaCursosPeriodos() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        JList<Curso> listaCursos = new JList<>(modeloLista(controller.listarCursos()));
        listaCursos.setBorder(BorderFactory.createTitledBorder("Cursos"));
        painel.add(new JScrollPane(listaCursos), BorderLayout.CENTER);

        JPanel formularios = new JPanel(new GridLayout(1, 2, 10, 10));
        formularios.add(formularioNovoCurso());
        formularios.add(formularioNovoPeriodo());
        painel.add(formularios, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel formularioNovoCurso() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Novo Curso"));
        GridBagConstraints gbc = criarGbc();

        JTextField nome = new JTextField(12);
        JTextField sigla = new JTextField(8);
        JTextField area = new JTextField("Ciencias Exatas", 12);
        JTextField carga = new JTextField("3600", 8);
        JTextField coordenador = new JTextField(12);
        JButton confirmar = botao("Confirmar");
        JButton limpar = botao("Limpar");

        adicionarLinha(painel, gbc, 0, "Nome:", nome);
        adicionarLinha(painel, gbc, 1, "Sigla:", sigla);
        adicionarLinha(painel, gbc, 2, "Area do conhecimento:", area);
        adicionarLinha(painel, gbc, 3, "Carga horaria:", carga);
        adicionarLinha(painel, gbc, 4, "Coordenador:", coordenador);
        adicionarComponente(painel, gbc, 5, painelBotoes(confirmar, limpar));

        confirmar.addActionListener(e -> tentar(() -> {
            controller.criarCurso(usuarioLogado, nome.getText(), sigla.getText(), area.getText(), Integer.parseInt(carga.getText()), coordenador.getText());
            mostrarPainelDev();
        }));
        limpar.addActionListener(e -> {
            nome.setText("");
            sigla.setText("");
            coordenador.setText("");
        });
        return painel;
    }

    private JPanel formularioNovoPeriodo() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Novo Periodo"));
        GridBagConstraints gbc = criarGbc();

        JComboBox<Curso> curso = new JComboBox<>(controller.listarCursos().toArray(new Curso[0]));
        JTextField numero = new JTextField("1", 8);
        JTextField nome = new JTextField(12);
        JButton confirmar = botao("Confirmar");
        JButton cancelar = botao("Cancelar");

        adicionarLinha(painel, gbc, 0, "Curso:", curso);
        adicionarLinha(painel, gbc, 1, "Periodo:", numero);
        adicionarLinha(painel, gbc, 2, "Nome:", nome);
        adicionarComponente(painel, gbc, 3, painelBotoes(confirmar, cancelar));

        confirmar.addActionListener(e -> tentar(() -> {
            controller.criarPeriodo(usuarioLogado, (Curso) curso.getSelectedItem(), Integer.parseInt(numero.getText()), nome.getText());
            mostrarPainelDev();
        }));
        cancelar.addActionListener(e -> nome.setText(""));
        return painel;
    }

    private JPanel abaGestaoDisciplinas() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JComboBox<Curso> cursos = new JComboBox<>(controller.listarCursos().toArray(new Curso[0]));
        JComboBox<Periodo> periodos = new JComboBox<>();
        DefaultListModel<Materia> modelo = new DefaultListModel<>();
        JList<Materia> lista = new JList<>(modelo);
        lista.setBorder(BorderFactory.createTitledBorder("Disciplinas:"));

        Runnable atualizarPeriodos = () -> {
            periodos.removeAllItems();
            Curso curso = (Curso) cursos.getSelectedItem();
            if (curso != null) {
                for (Periodo periodo : curso.getPeriodos()) {
                    periodos.addItem(periodo);
                }
            }
        };
        Runnable atualizarMaterias = () -> {
            modelo.clear();
            Periodo periodo = (Periodo) periodos.getSelectedItem();
            if (periodo != null) {
                for (Materia materia : periodo.getMaterias()) {
                    modelo.addElement(materia);
                }
            }
        };

        cursos.addActionListener(e -> {
            atualizarPeriodos.run();
            atualizarMaterias.run();
        });
        periodos.addActionListener(e -> atualizarMaterias.run());
        atualizarPeriodos.run();
        atualizarMaterias.run();

        filtros.add(new JLabel("Curso:"));
        filtros.add(cursos);
        filtros.add(new JLabel("Periodo:"));
        filtros.add(periodos);

        JPanel cadastro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cadastro.setBorder(BorderFactory.createTitledBorder("Cadastro"));
        JTextField nome = new JTextField(18);
        JComboBox<Professor> professores = new JComboBox<>(controller.listarProfessores().toArray(new Professor[0]));
        JButton confirmar = botao("Confirmar");
        JButton cancelar = botao("Cancelar");
        JButton remover = botao("Remover");
        cadastro.add(new JLabel("Nome da disciplina:"));
        cadastro.add(nome);
        cadastro.add(new JLabel("Professor:"));
        cadastro.add(professores);
        cadastro.add(confirmar);
        cadastro.add(cancelar);
        cadastro.add(remover);

        confirmar.addActionListener(e -> tentar(() -> {
            controller.criarMateria(usuarioLogado, (Periodo) periodos.getSelectedItem(), nome.getText(), (Professor) professores.getSelectedItem());
            atualizarMaterias.run();
            nome.setText("");
        }));
        cancelar.addActionListener(e -> nome.setText(""));
        remover.addActionListener(e -> tentar(() -> {
            Periodo periodo = (Periodo) periodos.getSelectedItem();
            Materia materia = lista.getSelectedValue();
            if (periodo == null || materia == null) {
                throw new RuntimeException("Escolha uma disciplina.");
            }
            periodo.removerMateria(materia);
            atualizarMaterias.run();
        }));

        painel.add(filtros, BorderLayout.NORTH);
        painel.add(new JScrollPane(lista), BorderLayout.CENTER);
        painel.add(cadastro, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel abaGestaoUsuarios() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        String[] colunas = {"Nome", "Login", "Senha", "Cargo"};
        DefaultTableModel tabelaModelo = new DefaultTableModel(colunas, 0);
        for (Usuario usuario : controller.listarUsuarios()) {
            tabelaModelo.addRow(new Object[]{usuario.getNome(), usuario.getLogin(), usuario.getSenha(), usuario.getCargo()});
        }
        JTable tabela = new JTable(tabelaModelo);
        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel cadastro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cadastro.setBorder(BorderFactory.createTitledBorder("Cadastrar Usuarios"));
        JTextField nome = new JTextField(12);
        JTextField login = new JTextField(10);
        JPasswordField senha = new JPasswordField(10);
        JComboBox<String> cargo = new JComboBox<>(new String[]{"Docente", "Monitor", "Aluno"});
        JButton cadastrar = botao("Cadastrar");
        JButton remover = botao("Remover");

        cadastro.add(new JLabel("Nome:"));
        cadastro.add(nome);
        cadastro.add(new JLabel("Login:"));
        cadastro.add(login);
        cadastro.add(new JLabel("Senha:"));
        cadastro.add(senha);
        cadastro.add(new JLabel("Cargo:"));
        cadastro.add(cargo);
        cadastro.add(cadastrar);
        cadastro.add(remover);

        cadastrar.addActionListener(e -> tentar(() -> {
            controller.cadastrarUsuarioPeloCargo(usuarioLogado, nome.getText(), login.getText(), new String(senha.getPassword()), (String) cargo.getSelectedItem());
            mostrarPainelDev();
        }));

        remover.addActionListener(e -> tentar(() -> {
            int linha = tabela.getSelectedRow();
            if (linha < 0) {
                throw new RuntimeException("Escolha um usuario.");
            }
            controller.removerUsuario(usuarioLogado, controller.listarUsuarios().get(linha));
            mostrarPainelDev();
        }));

        painel.add(cadastro, BorderLayout.SOUTH);
        return painel;
    }

    private void mostrarMateria() {
        JPanel painel = painelComMargem();
        painel.add(cabecalho(""), BorderLayout.NORTH);

        JTabbedPane abas = new JTabbedPane();
        if (usuarioLogado.getNivel() >= 2) {
            abas.addTab("Dashboard", abaDashboard());
        } else {
            abas.addTab("Feedback de Aulas", abaFeedback());
        }

        if (usuarioLogado.getNivel() >= 3) {
            abas.addTab("Gestao de aulas", abaGestaoAulas());
            abas.addTab("Gestao de monitores", abaGestaoMonitores());
        }

        abas.addTab("Repositorio de Arquivos", abaRepositorio());

        if (usuarioLogado.getNivel() >= 2) {
            abas.addTab("Chat de equipe", painelChat("Chat de Equipe", materiaAberta.getChatEquipe()));
        }

        abas.addTab("Chat geral", painelChat("Chat geral", materiaAberta.getChatGeral()));
        abas.addTab("Chats por aula", abaChatsPorAula());

        painel.add(abas, BorderLayout.CENTER);
        mostrarTela(painel);
    }

    private JPanel abaDashboard() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        JLabel titulo = new JLabel("Dashboard");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        painel.add(titulo, BorderLayout.NORTH);

        String[] colunas = {"Aula", "Data", "Respostas", "Media da turma", "Status da turma"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0);
        int totalAlunos = materiaAberta.getEstudantes().size();
        for (Aula aula : materiaAberta.getAulas()) {
            int respostas = aula.getFeedbacks().size();
            String media = respostas == 0 ? "N/A" : String.format("%.1f", aula.calcularMedia());
            modelo.addRow(new Object[]{
                    aula.getTitulo(),
                    aula.getData(),
                    respostas + "/" + totalAlunos,
                    media,
                    aula.getStatus()
            });
        }
        painel.add(new JScrollPane(new JTable(modelo)), BorderLayout.CENTER);
        return painel;
    }

    private JPanel abaFeedback() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Feedback de Aulas"));
        GridBagConstraints gbc = criarGbc();

        JComboBox<Aula> aulas = new JComboBox<>(materiaAberta.getAulas().toArray(new Aula[0]));
        JSlider nivel = new JSlider(0, 10, 5);
        nivel.setMajorTickSpacing(1);
        nivel.setPaintTicks(true);
        nivel.setPaintLabels(true);
        JTextField comentario = new JTextField(20);
        JButton registrar = botao("Registrar");
        JButton mostrar = botao("Mostrar feedback");

        adicionarLinha(painel, gbc, 0, "Aula:", aulas);
        adicionarLinha(painel, gbc, 1, "Nivel de Compreensao:", nivel);
        adicionarLinha(painel, gbc, 2, "Comentario:", comentario);
        adicionarComponente(painel, gbc, 3, painelBotoes(registrar, mostrar));

        registrar.addActionListener(e -> tentar(() -> {
            controller.registrarFeedback(usuarioLogado, (Aula) aulas.getSelectedItem(), nivel.getValue(), comentario.getText());
            JOptionPane.showMessageDialog(this, "Feedback registrado.");
        }));
        mostrar.addActionListener(e -> {
            Aula aula = (Aula) aulas.getSelectedItem();
            if (aula != null) {
                JOptionPane.showMessageDialog(this, "Media atual: " + String.format("%.1f", aula.calcularMedia()) + "\nStatus: " + aula.getStatus());
            }
        });
        return painel;
    }

    private JPanel abaGestaoAulas() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        JList<Aula> lista = new JList<>(modeloLista(materiaAberta.getAulas()));
        painel.add(new JScrollPane(lista), BorderLayout.CENTER);

        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formulario.setBorder(BorderFactory.createTitledBorder("Gestao de aulas"));
        JTextField titulo = new JTextField(16);
        JTextField data = new JTextField(10);
        JButton criar = botao("Criar Aula");
        JButton remover = botao("Remover");

        formulario.add(new JLabel("Titulo da aula:"));
        formulario.add(titulo);
        formulario.add(new JLabel("Data:"));
        formulario.add(data);
        formulario.add(criar);
        formulario.add(remover);

        criar.addActionListener(e -> tentar(() -> {
            controller.criarAula(usuarioLogado, materiaAberta, titulo.getText(), data.getText());
            mostrarMateria();
        }));
        remover.addActionListener(e -> tentar(() -> {
            Aula aula = lista.getSelectedValue();
            if (aula == null) {
                throw new RuntimeException("Escolha uma aula.");
            }
            materiaAberta.getAulas().remove(aula);
            mostrarMateria();
        }));

        painel.add(formulario, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel abaGestaoMonitores() {
        JPanel painel = new JPanel(new GridLayout(1, 2, 10, 10));

        JPanel listaPainel = new JPanel(new BorderLayout());
        listaPainel.setBorder(BorderFactory.createTitledBorder("Gestao de monitores"));
        JList<Monitor> lista = new JList<>(modeloLista(materiaAberta.getMonitores()));
        JButton remover = botao("Remover item");
        listaPainel.add(new JScrollPane(lista), BorderLayout.CENTER);
        listaPainel.add(remover, BorderLayout.SOUTH);

        JPanel cadastro = new JPanel(new GridBagLayout());
        cadastro.setBorder(BorderFactory.createTitledBorder("Cadastrar monitores"));
        GridBagConstraints gbc = criarGbc();
        JTextField nome = new JTextField(12);
        JTextField login = new JTextField(12);
        JPasswordField senha = new JPasswordField(12);
        JTextField tipo = new JTextField("Monitor", 12);
        JButton confirmar = botao("Confirmar");

        adicionarLinha(cadastro, gbc, 0, "Nome:", nome);
        adicionarLinha(cadastro, gbc, 1, "Login:", login);
        adicionarLinha(cadastro, gbc, 2, "Senha:", senha);
        adicionarLinha(cadastro, gbc, 3, "Cargo:", tipo);
        adicionarComponente(cadastro, gbc, 4, confirmar);

        confirmar.addActionListener(e -> tentar(() -> {
            Monitor monitor = controller.cadastrarMonitor(usuarioLogado, nome.getText(), login.getText(), login.getText() + "@minddebug.edu.br", new String(senha.getPassword()), tipo.getText());
            controller.adicionarMonitorNaMateria(usuarioLogado, materiaAberta, monitor);
            mostrarMateria();
        }));
        remover.addActionListener(e -> tentar(() -> {
            Monitor monitor = lista.getSelectedValue();
            if (monitor == null) {
                throw new RuntimeException("Escolha um monitor.");
            }
            materiaAberta.removerParticipante(monitor);
            mostrarMateria();
        }));

        painel.add(cadastro);
        painel.add(listaPainel);
        return painel;
    }

    private JPanel abaRepositorio() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(BorderFactory.createTitledBorder("Repositorio de Arquivos"));

        JList<Material> lista = new JList<>(modeloLista(materiaAberta.getMateriais()));
        painel.add(new JScrollPane(lista), BorderLayout.CENTER);

        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField novoItem = new JTextField(25);
        JButton adicionarLink = botao("Adicionar Link");
        JButton enviarArquivo = botao("Enviar Arquivo");
        JButton apagar = botao("Apagar Selecionado");

        formulario.add(new JLabel("Novo Item:"));
        formulario.add(novoItem);
        formulario.add(adicionarLink);
        formulario.add(enviarArquivo);
        formulario.add(apagar);

        adicionarLink.addActionListener(e -> tentar(() -> {
            controller.adicionarMaterial(materiaAberta, usuarioLogado, "Link", "Link", novoItem.getText());
            mostrarMateria();
        }));
        enviarArquivo.addActionListener(e -> tentar(() -> {
            JFileChooser seletor = new JFileChooser();
            if (seletor.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File arquivo = seletor.getSelectedFile();
                controller.adicionarMaterial(materiaAberta, usuarioLogado, arquivo.getName(), "Arquivo", arquivo.getName());
                mostrarMateria();
            }
        }));
        apagar.addActionListener(e -> tentar(() -> {
            controller.removerMaterial(materiaAberta, usuarioLogado, lista.getSelectedValue());
            mostrarMateria();
        }));

        painel.add(formulario, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel abaChatsPorAula() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<Aula> aulas = new JComboBox<>(materiaAberta.getAulas().toArray(new Aula[0]));
        JPanel areaChat = new JPanel(new BorderLayout());

        Runnable atualizar = () -> {
            areaChat.removeAll();
            Aula aula = (Aula) aulas.getSelectedItem();
            if (aula != null) {
                areaChat.add(painelChat("", aula.getChat()), BorderLayout.CENTER);
            }
            areaChat.revalidate();
            areaChat.repaint();
        };

        aulas.addActionListener(e -> atualizar.run());
        topo.add(new JLabel("Aula:"));
        topo.add(aulas);
        painel.add(topo, BorderLayout.NORTH);
        painel.add(areaChat, BorderLayout.CENTER);
        atualizar.run();
        return painel;
    }

    private JPanel painelChat(String titulo, Chat chat) {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        if (titulo != null && !titulo.isEmpty()) {
            JLabel label = new JLabel(titulo);
            label.setFont(new Font("Arial", Font.BOLD, 18));
            painel.add(label, BorderLayout.NORTH);
        }

        JList<Mensagem> lista = new JList<>(modeloLista(chat.getMensagens()));
        painel.add(new JScrollPane(lista), BorderLayout.CENTER);

        JPanel rodape = new JPanel(new BorderLayout(8, 8));
        JTextField texto = new JTextField();
        JButton enviar = botao("Enviar");
        JButton apagar = botao("Apagar mensagem");
        JPanel botoes = painelBotoes(apagar, enviar);

        rodape.add(texto, BorderLayout.CENTER);
        rodape.add(botoes, BorderLayout.EAST);

        enviar.addActionListener(e -> tentar(() -> {
            controller.enviarMensagem(chat, usuarioLogado, texto.getText());
            mostrarMateria();
        }));
        apagar.addActionListener(e -> tentar(() -> {
            controller.apagarMensagem(chat, usuarioLogado, lista.getSelectedValue());
            mostrarMateria();
        }));

        painel.add(rodape, BorderLayout.SOUTH);
        return painel;
    }

    private void trocarSenha() {
        JPasswordField senha = new JPasswordField(12);
        int resposta = JOptionPane.showConfirmDialog(this, senha, "Nova senha", JOptionPane.OK_CANCEL_OPTION);
        if (resposta == JOptionPane.OK_OPTION) {
            tentar(() -> {
                usuarioLogado.trocarSenha(new String(senha.getPassword()));
                JOptionPane.showMessageDialog(this, "Senha alterada.");
            });
        }
    }

    private JPanel painelBotoes(JButton... botoes) {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        for (JButton botao : botoes) {
            painel.add(botao);
        }
        return painel;
    }

    private GridBagConstraints criarGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void adicionarLinha(JPanel painel, GridBagConstraints gbc, int linha, String texto, JComponent componente) {
        gbc.gridy = linha;
        gbc.gridx = 0;
        gbc.weightx = 0;
        painel.add(new JLabel(texto), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        painel.add(componente, gbc);
    }

    private void adicionarComponente(JPanel painel, GridBagConstraints gbc, int linha, JComponent componente) {
        gbc.gridy = linha;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        painel.add(componente, gbc);
        gbc.gridwidth = 1;
    }

    private <T> DefaultListModel<T> modeloLista(ArrayList<T> itens) {
        DefaultListModel<T> modelo = new DefaultListModel<>();
        for (T item : itens) {
            modelo.addElement(item);
        }
        return modelo;
    }
}
