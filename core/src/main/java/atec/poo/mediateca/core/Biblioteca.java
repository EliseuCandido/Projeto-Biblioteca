package atec.poo.mediateca.core;

import atec.poo.mediateca.core.exceptions.*;
import atec.poo.mediateca.core.utilidades.CompareObraByID;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

public class Biblioteca implements Serializable {
    private static final long serialVersionUID = 2L;
    private HashMap<Integer, User> users;
    private HashMap<Integer, Obra> obras;
    private int nextUserID;
    private int nextObraID;
    private int data;

    /**
     * Construtor
     */
    public Biblioteca() {
        this.users = new HashMap<>();
        this.obras = new HashMap<>();
        this.nextUserID = 1;
        this.nextObraID = 1;
        this.data = 0;
    }

    /**
     * Define a Data Atual
     * @param data
     */
    public void setData(int data) {
        if (data > 0)
            this.data += data;
    }

    /**
     * Mostra a Data Atual
     * @return Data Atual
     */
    public int getData() {
        return data;
    }

    /**
     * Registra um novo utente na biblioteca.
     * @param nome O nome do utente.
     * @param email O email do utente.
     * @return ID do novo utente criado
     */
    public int registarUser(String nome, String email) {
        User u = new User(this.nextUserID, nome, email);
        this.users.put(u.getId(), u);
        this.nextUserID++;
        return u.getId();
    }

    /**
     * Mostra informações sobre todos os utentes
     * @return As informações de todos os utentes
     */
    public ArrayList<User> listUsers() {
        ArrayList<User> users_array = new ArrayList<>(this.users.values());
        Collections.sort(users_array);
        return users_array;
    }

    /**
     * Obtém informações sobre um utente específico.
     * @param id O ID do utente.
     * @return As informações do utente pretendido.
     * @throws UserNotFoundException Se o usuário não existe.
     */
    public String mostrarUtente(int id) throws UserNotFoundException {
        if (this.users.containsKey(id))
            return this.users.get(id).toString();
        throw new UserNotFoundException(id);
    }

    /**
     * Obtém notificações de um utente específico.
     * @param id O ID do utente.
     * @return As notificações do utente pretendido.
     * @throws UserNotFoundException Se o usuário não existe.
     */
    public String mostrarNotificacao(int id) throws UserNotFoundException {
        if (this.users.containsKey(id))
            return this.users.get(id).toString(); // Em vez mostrar informação do Utente mostrar as notificações desse Utente (Entrega/Requisição)
            // Exemplos:
            /*
                ENTREGA: 4 - 2 de 4 - DVD - Casamento Real - 8 Ficção - António Fonseca - 200400500
                REQUISIÇÃO: 5 - 4 de 22 - Livro - Dicionário - 45 - Referência - Pedro Casanova - 1234567893
             */
        throw new UserNotFoundException(id);
    }

    /**
     *
     * @param userID
     */

    public void pagarMulta(int userID){
        User user = this.users.get(userID);
        if (user.getEstado().toString().equals("SUSPENSO")) {  // A Multa é saldada e user passa ativo!
            user.setMulta(0);
            user.setEstado(Estado.valueOf("ACTIVO"));
        }
        if (user.getEstado().toString().equals("ACTIVO")) {
            user.setMulta(0);
        }
    }

    /**
     * Registra um novo livro na biblioteca.
     * @param titulo O titulo do livro.
     * @param autor O autor do livro.
     * @param preco O preco do livro.
     * @param categoria A categoria do livro.
     * @param isbn O valor de ISBN do livro.
     * @param exemplares Nº de Exemplares do livro.
     * @return ID do novo livro criado
     */
    public int registarLivro(String titulo, String autor, Double preco, String categoria, String isbn, int exemplares) {
        Livro l = new Livro(this.nextObraID, titulo, autor, preco, categoria, isbn, exemplares);
        this.obras.put(l.getId(), l);
        this.nextObraID++;
        return l.getId();
    }

    /**
     * Registra um novo dvd na biblioteca.
     * @param titulo O titulo do dvd.
     * @param realizador O realizador do dvd.
     * @param preco O preco do dvd.
     * @param categoria A categoria do dvd.
     * @param igac O valor de IGAC do dvd.
     * @param exemplares Nº de Exemplares do dvd.
     * @return ID do novo dvd criado
     */
    public int registarDVD(String titulo, String realizador, Double preco, String categoria, String igac, int exemplares) {
        DVD d = new DVD(this.nextObraID, titulo, realizador, preco, categoria, igac, exemplares);
        this.obras.put(d.getId(), d);
        this.nextObraID++;
        return d.getId();
    }

    /**
     * Obtém informações sobre uma obra específica.
     * @param id
     * @return As informações da obra pretendido.
     */
    public String mostrarObra(int id) throws WorkNotFoundException {
        if (this.obras.containsKey(id))
            return this.obras.get(id).toString();
        throw new WorkNotFoundException(id);
    }


    /**
     * Mostra informações sobre todas as obras por ordem crescente do ID da obra
     * @return As informações de todas as obras
     */
    public ArrayList<Obra> listObrasByID() {
        ArrayList<Obra> obras_array = new ArrayList<>(this.obras.values());
        Collections.sort(obras_array,new CompareObraByID()); //A ordem vai ser por ID
        return obras_array;
    }

    /**
     * Requisita obra pelo o id do Utente que quer requisitar e o id da Obra a ser requisitada
     * @param userID Utilizador que vai fazer a solicitação da Obra
     * @param obraID Obra a ser Requisitada
     */
    public void requisitarObra(int userID, int obraID) throws RuleException {
        Obra obra = this.obras.get(obraID);
        User user = this.users.get(userID);

        if (user.getEstado().toString().equals("SUSPENSO")) {
            throw new RuleException(userID, obraID, 2);
        }

        if (obra.getStock() <= 0) {
            throw new RuleException(userID, obraID, 3);
        }

        if (obra.getCategoria().equals("REFERENCE"))
            throw new RuleException(userID, obraID, 5);

        if (obra.getPreco() > 25.00 && !user.getComportamento().equals("CUMPRIDOR"))
            throw new RuleException(userID, obraID, 6);

        if (!user.getObraID(obraID)) {
            String comportamento = user.getComportamento().toString();
            int requisicaoLimite = 0;

            switch (comportamento) {
                case "NORMAL":
                    requisicaoLimite = 3;
                    break;
                case "CUMPRIDOR":
                    requisicaoLimite = 5;
                    break;
                case "FALTOSO":
                    requisicaoLimite = 1;
                    break;
            }

            if (user.numRequisicoes < requisicaoLimite) {
                user.numRequisicoes++;
                user.requisicao.add(obraID);
                int novoStock = obra.getStock() - 1;
                obra.setStock(novoStock);
                user.setMulta(39);
            } else {
                throw new RuleException(userID, obraID, 4);
            }
        } else {
            throw new RuleException(userID, obraID, 1);
        }
    }

    /**
     *
     * @param userID
     * @param obraID
     * @return
     */
    public int requisicaoMaxDias (int userID, int obraID) {
        User user = users.get(userID);
        Obra obra = obras.get(obraID);
        if (obra.getExemplares() > 5) {
            return switch (user.getComportamento()) {
                case CUMPRIDOR -> 30;
                case NORMAL -> 15;
                case FALTOSO -> 2;
            };
        } else if (obra.getExemplares() > 1 && obra.getExemplares() <= 5) {
            return switch (user.getComportamento()) {
                case CUMPRIDOR -> 15;
                case NORMAL -> 8;
                case FALTOSO -> 2;
            };
        } else if (obra.getExemplares() == 1) {
            return switch (user.getComportamento()) {
                case CUMPRIDOR -> 8;
                case NORMAL -> 3;
                case FALTOSO -> 2;
            };
        }
        return 0;
    }

    /**
     *
     * @param userID
     * @param obraID
     * @return
     */
    public String devolverObra(int userID, int obraID) {
        User user = this.users.get(userID);
        Obra obra = this.obras.get(obraID);

        user.numRequisicoes--;
        user.requisicao.remove(Integer.valueOf(obraID));
        int novoStock = obra.getStock() + 1;
        obra.setStock(novoStock);

        return "[Obra devolvida com Sucesso]" + "\nUtente: " + user.getNome() + "\nObra: " + obra.getTitulo(); //Alterar mensagem
    }

    /**
     *
     * @param userID
     * @param obraID
     * @return
     * @throws BorrowException
     */
    public String ver_utente_obra(int userID, int obraID) throws BorrowException{
        User user = this.users.get(userID);

        if (user.getObraID(obraID)) {
            return this.obras.get(obraID).toString();
        }
        throw new BorrowException(userID,obraID);
    }

    /**
     *
     * @param userID
     * @return
     */
    public int mostraMulta(int userID){
        User user = this.users.get(userID);
            return user.getMulta();
    }

    /**
     * Read the text input file at the beginning of the program and populates the
     * instances of the various possible types (books, DVDs, users).
     *
     * @param filename of the file to load
     * @throws BadEntrySpecificationException A especificação do ficheiro não é correta
     * @throws IOException                    Erro na abertura e/ou Leitura do ficheiro
     */
    void importFile(String filename) throws BadEntrySpecificationException, IOException {
        Scanner s = new Scanner(new File(filename));
        while (s.hasNextLine()) {
            String line = s.nextLine();
            //System.out.println(line);
            String[] elementos = line.split(":", 0);
            switch (elementos[0]) {

                case "USER":
                    this.registarUser(elementos[1], elementos[2]);
                    break;
                case "BOOK":
                    this.registarLivro(elementos[1],elementos[2],Double.parseDouble(elementos[3]),elementos[4],elementos[5],Integer.parseInt(elementos[6]));
                    break;
                case "DVD":
                    this.registarDVD(elementos[1],elementos[2],Double.parseDouble(elementos[3]),elementos[4],elementos[5],Integer.parseInt(elementos[6]));
                    break;
                default:
                    throw new BadEntrySpecificationException("Unknow type of category");
            }
        }
        s.close();
    }
}