package project;

import java.util.Arrays;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Criação e iteração de autómatos celulares.
 *
 * Na versão actual, está implementado o Jogo da Vida, de Conway, com a opção de
 * configurar variantes não-standard do mesmo através da regra de actualização
 * de células que é passada aos métodos de iteração do mundo.
 *
 * @author csl
 */
public class Mundo {
	
	private int[][] mundo;  // int desperdiça muita memória, mas facilita alteração para outros autómatos celulares
	private int numLinhas;  // redundante, mas de acordo com o padrão "explanatory variable"
	private int numColunas; // redundante, mas de acordo com o padrão "explanatory variable"

	/**
	 * Inicializa o mundo a partir de uma matriz de 0s e 1s.
	 * 
	 * @param mundoInicial matriz constituída por 0s e 1s
	 * @requires mundoInicial.length >= 2
	 * @requires mundoInicial[0].length >= 2
	 */
	public Mundo(int[][] mundoInicial) {
		//Atribuir valores as variavies globais
		numLinhas = mundoInicial.length;
		numColunas = mundoInicial[0].length;
		if (numLinhas < 2 || numColunas < 2) {
            throw new IllegalArgumentException("Mundo demasiado pequeno!");
        }
		//verificar o tamanho do tabuleiro antes de proseeguir
		mundo = mundoInicial;
	}
	
	/**
	 * Inicializa o mundo a partir de um ficheiro de texto.
	 * 
	 * O tamanho do mundo a inicializar é determinado pelo número de linhas e de "colunas"
	 * do ficheiro.
	 * 
	 * @param ficheiroMundoInicial nome (ou path) do ficheiro de inicialização
	 * @requires os dados têm de estar armazenados no ficheiro no mesmo formato
	 *   usado por escreveMundo()
	 * @requires o ficheiro tem de ter pelo menos 2 linhas com dados válidos,
	 *   permitindo assim criar um mundo com pelo menos 2 linhas
	 * @requires o ficheiro tem de ter pelo menos 2 "colunas" com dados válidos,
	 *   permitindo assim criar um mundo com pelo menos 2 colunas
	 * @requires robustez: o ficheiro pode terminar com uma mudança de linha, ou não;
	 *   isso não deve afectar o mundo lido nem o respectivo tamanho
	 */
	public Mundo(String ficheiroMundoInicial) throws IOException {
		try {
			//Abrir scanner direcionado para ficheiroMundoInicial
			Scanner sc = new Scanner(new File(ficheiroMundoInicial));
			//Variavel de apoio
			int[][] mundoInicial = new int[0][0];
			//Tratar da primeira linha do mundo
			if (sc.hasNextLine()) {
				//Dividir as posicoes da linha num array para verificar o que representam (0 ou 1)
				String[] primeiraLinha = sc.nextLine().split(" ");
				//Inicializar a variavel de apoio com 1 linha e com as colunas ja necessárias
				mundoInicial = new int[1][primeiraLinha.length];
				//Percorrer a linha e atrivuir o respetivo valor a posicao correspondente na varivael de apoio
				for (int i = 0; i < primeiraLinha.length; i++) {
					//X representa 1
					if (primeiraLinha[i].equals("X")) {
						mundoInicial[0][i] = 1;
					//. representa 0
					} else {
						mundoInicial[0][i] = 0;
					}
				}
			}
		//Ciclo para tratar das restantes linhas
		while (sc.hasNextLine()) {
			//Dividir as posicoes da linha num array para verificar o que representam (0 ou 1)
			String[] linha = sc.nextLine().split(" ");
			//Inicializar uma nova varivel de apoio ccom base na anterior
			int[][] novoMundoInicial = new int[mundoInicial.length + 1][linha.length];
			//Preencher os dados da V.A. com base na V.A. anterior 
			for(int i = 0; i < mundoInicial.length; i++){
				novoMundoInicial[i] = mundoInicial[i];
			}
			//Percorrer a linha e atrivuir o respetivo valor a posicao correspondente na varivael de apoio
			for (int i = 0; i < linha.length; i++) {
				//. representa 0
				if (linha[i].equals("X")) {
				novoMundoInicial[mundoInicial.length][i] = 1;
				//. representa 0
				} else {
				novoMundoInicial[mundoInicial.length][i] = 0;
				}
			}
			//atribuir o valor da V.A. nova a anterior para reutilizar
			mundoInicial = novoMundoInicial;
		}
		//fechar canal de comunicacao 
		sc.close();
		//Atribuir valores as variavies globais
		numLinhas = mundoInicial.length;
		numColunas = mundoInicial[0].length;
		//verificar o tamanho do tabuleiro antes de proseeguir
		if (numLinhas < 2 || numColunas < 2) {
            throw new IllegalArgumentException("Mundo demasiado pequeno!");
        }
		mundo = mundoInicial;

		} catch (FileNotFoundException e) {
			System.out.println("Ficheiro nao encontrado, tente novamente!");
		}
		

	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Mundo other = (Mundo) obj;
		return Arrays.deepEquals(mundo, other.mundo);
	}

	/**
	 * Determina se uma célula vive na geração seguinte.
	 *
	 * submatriz é uma matriz quadrada com 9 elementos, onde o elemento central é
	 * a célula cuja vida é determinada de acordo com a regra escolhida.
	 * 1 denota uma célula viva, 0 denota uma célula morta.
	 * Seja regra = {maxVizinhos, minVizinhos, vizinhosParaNascer}.
	 * Uma célula viva:
	 * -- morre se tiver mais de maxVizinhos vivos;
	 * -- morre se tiver menos de minVizinhos vivos;
	 * -- permanece viva nos restantes casos.
	 * Uma célula morta:
	 * -- renasce se tiver exactamente vizinhosParaNascer vizinhos vivos;
	 * -- permanece morta nos restantes casos.
	 * 
	 * @param submatriz matriz 3x3 de 0s e 1s
	 * @param regra array com 3 elementos
	 * @requires cada elemento de regra está entre 0 e 8 inclusive
	 * @return true se a célula central fica viva com a regra escolhida, false caso contrário
	 */
	// visibilidade poderia ser private ou package, mas fica public por simplicidade
	public static boolean celulaVive(int[][] submatriz, int[] regra) {
		//Verificar quantos vizinhos vivos tem a celula central
		int vizinhosVivos = 0; 
		for(int i = 0; i < submatriz.length; i++){
			for(int j = 0; j < submatriz[i].length; j++){
				if(submatriz[i][j] == 1){
					vizinhosVivos++;
				}
			}	
		}
		//Verificar se celula esta viva no inicio e aplicar as leis para entender se se mantém viva
		if(submatriz[1][1] == 1){
			//Retirar a propria celula da conta vizinhosVivos
			vizinhosVivos--;
			//Regras para morrer caso esteja viva
			if(vizinhosVivos > regra[0] || vizinhosVivos < regra[1]){
				return false;
			} else {
				return true;
			}
		//No caso da celula estar morta verificar se renasce
		} else {
			//Verificar se o numero de vizinhos vivos eh suficiente para renascer
			if(vizinhosVivos == regra[2]){
				return true;
			} else {
				return false;
			}

		}
	}
	
	public int getNumLinhas() {
		return numLinhas;
	}
	
	public int getNumColunas() {
		return numColunas;
	}
	
	/**
	 * Reinicializa o mundo, tornando todas as suas células mortas.
	 */
	public void zeraMundo() {
		//Itera todo o array mundo e coloca todas as posições a 0
		for(int i = 0; i < mundo.length; i++){
			for(int j = 0; j < mundo[i].length; j++){
				atribuiValorCelula(i, j, 0);
			}	
		}
	}
	
	/**
	 * Atribui um valor a uma célula.
	 *
	 * Força a atribuição de um valor à célula do mundo identificada pela respectiva posição.
	 * 
	 * @param linha índice da linha da célula que se quer afectar
	 * @param coluna índice da coluna da célula que se quer afectar
	 * @param valor a atribuir à célula
	 * @requires {@literal 0 <= linha < numLinhas}
	 * @requires {@literal 0 <= coluna < numColunas}
	 * @requires valor é 0 ou 1
	 */
	public void atribuiValorCelula(int linha, int coluna, int valor) {
		if (linha < 0 || coluna < 0 || linha > numLinhas || coluna > numColunas ) {
            throw new IllegalArgumentException("Erro no valor linha ou coluna");
        } else if (valor < 0 || valor > 1){
			throw new IllegalArgumentException("Erro, valor deve ser 0 ou 1");
		} else {
			mundo[linha][coluna] = valor;
		}
	}
	
	/**
	 * Devolve o valor de uma célula do mundo.
	 *
	 * @param linha índice da linha da célula cujo valor se quer obter
	 * @param coluna índice da coluna da célula cujo valor se quer obter
	 * @requires {@literal 0 <= linha < numLinhas}
	 * @requires {@literal 0 <= coluna < numColunas}
	 * @return 0 ou 1, conforme o valor da célula
	 */
	public int valorDaCelula(int linha, int coluna) {
		if (linha < 0 || coluna < 0 || linha > numLinhas || coluna > numColunas ) {
            throw new IllegalArgumentException("Erro no valor linha ou coluna");
        } else {
			return mundo[linha][coluna];
		}
	}
	
	/**
	 * Actualiza o estado de todas as células do mundo, de acordo com a regra.
	 * 
	 * Define o valor de cada célula do mundo para a iteração seguinte com a regra dada,
	 * alterando esse valor onde necessário.
	 * As condições de fronteira a concretizar são periódicas.
	 *  
	 * @param regra array com 3 elementos
	 * @requires cada elemento de regra está entre 0 e 8 inclusive
	 */
	// Sugestões (opcionais) de implementação.
	// Devido a as condições de fronteira a concretizar serem periódicas,
	//  mundo[i][numColunas] deve replicar mundo[i][0]
	//  mundo[i][-1] deve replicar mundo[i][numColunas-1]
	//  mundo[numLinhas][j] deve replicar mundo[0][j]
	//  mundo[-1][j] deve replicar mundo[numLinhas-1][j]
	// Como as componentes de replicação estão FORA do mundo, considera-se que existem
	// numa MATRIZ AUMENTADA, com 1 linha acrescentada antes da primeira linha de mundo,
	// e 1 linha acrescentada após a última linha de mundo; idem para 1 coluna acrescentada
	// antes e outra depois das colunas originais do mundo.
	// No entanto, não é preciso criar explicitamente uma matriz aumentada. O fundamental
	// é que cada célula numa linha ou coluna limítrofe consiga encontrar os seus vizinhos
	// no lado oposto do mundo.
	public void iteraMundo(int[] regra) {
		// COMPLETAR
	}
	
	/**
	 * Itera o mundo n vezes, de acordo com a regra.
	 * 
	 * @param n número de iterações a efectuar
	 * @param regra array com 3 elementos
	 * @requires cada elemento de regra está entre 0 e 8 inclusive
	 */
	public void iteraMundoNgeracoes(int n, int[] regra) {
		// COMPLETAR
	}
	
	/**
	 * Mostra o mundo no output standard (ecrã, terminal, consola).
	 *
	 * Imprime a matriz mundo no ecrã, linha a linha, representando
	 *  cada 0 pela sequência ". " (ponto seguido de espaço) e
	 *  cada 1 pela sequência "X " (x maiúsculo seguido de espaço);
	 *  cada linha impressa termina com um espaço, que se segue a '.' ou a 'X'.
	 */
	public void mostraMundo() {
		for(int i = 0; i < mundo.length; i++){
			for(int j = 0; j < mundo[i].length; j++){
				if(valorDaCelula(i, j) == 0){
					System.out.print(". ");
				} else {
					System.out.print("X ");
				} 
			}
			System.out.println("\n");	
		}
	}

	/**
	 * Regista o mundo num ficheiro de texto.
	 *
	 * Comporta-se como mostraMundo, mas escrevendo num ficheiro em vez de no ecrã.
	 * Regista a matriz mundo no ficheiro, linha a linha, representando
	 *  cada 0 pela sequência ". " (ponto seguido de espaço) e
	 *  cada 1 pela sequência "X " (x maiúsculo seguido de espaço);
	 *  cada linha registada termina com um espaço, que se segue a '.' ou a 'X'.
	 * Se o ficheiro nomeFicheiro existir, ele é reescrito; caso contrário,
	 * é criado um novo ficheiro de texto com esse path.
	 * Nota de API: poderia ser apenas uma variante de mostraMundo sobrecarregada (overloaded),
	 * mas optou-se por métodos com nomes diferentes para escrever no output standard e em ficheiros.
	 * 
	 * @see mostraMundo() formato de armazenamento dos dados no ficheiro
	 * @param nomeFicheiro é uma string que representa um path válido para um ficheiro
	 */
	public void escreveMundo(String nomeFicheiro)  throws IOException {
		PrintWriter writer = new PrintWriter(new File(nomeFicheiro));
    	for(int i = 0; i < mundo.length; i++) {
        	for(int j = 0; j < mundo[i].length; j++) {
            	if(valorDaCelula(i, j) == 0){
                	writer.print(". ");
            	} else {
                writer.print("X ");
            	}
        	}
        	writer.print("\n");    
    	}
    	writer.close();
	}
		
	/**
	 * Representação deste mundo como uma string.
	 * 
	 * O formato da string é como descrito no contrato de mostraMundo.
	 * 
	 * @return uma string que termina com um carácter de mudança de linha
	 */
	@Override
	public String toString() {
		StringBuilder resultado = new StringBuilder();
		for(int i = 0; i < mundo.length; i++){
			for(int j = 0; j < mundo[i].length; j++){
				if(valorDaCelula(i, j) == 0){
					resultado.append(". ");
				} else {
					resultado.append("X ");
				} 
			}
			resultado.append("\n");	
		}
		return resultado.toString();
	}

	/**
	 * Mostra uma animação do mundo no output standard (ecrã).
	 *
	 * Imprime a condição inicial do mundo, e imprime depois
	 *    n sucessivos fotogramas (frames) de uma animação da evolução do mundo
	 *    a partir dessa condição inicial, usando a regra dada;
	 *  em cada frame, imprime a matriz mundo no ecrã, linha a linha,
	  *   representando
	 *    cada 0 pela string ". " (ponto seguido de espaço) e
	 *    cada 1 pela string "X " (x maiúsculo seguido de espaço);
	 *    cada linha impressa termina com um espaço, que se segue a '.' ou a 'X'.
	 *  antes de ser impresso o fotograma inicial (condição inicial), o ecrã é
	 *    apagado;
	 *  cada fotograma sobrepõe-se ao anterior, que foi entretanto apagado;
	 *  o tempo entre fotogramas é dado pelo valor de atraso em segundos;
	 *  este método altera o estado do mundo.
	 *  
	 * @param n número de iterações a efectuar
	 * @param regra array com 3 elementos
	 * @param atrasoEmSegundos intervalo de tempo entre fotogramas sucessivos
	 * @requires {@literal n > 0}
	 * @requires cada elemento de regra está entre 0 e 8 inclusive
	 * @requires {@literal atrasoEmSegundos > 0}
	 */
	// A implementação actual NÃO RESPEITA O CONTRATO no requisito de apagar o ecrã
	// antes da apresentação de cada frame.
	// Isso tem a ver com limitações da consola do Eclipse, onde é suposto o projecto ser
	// testado. Noutros ambientes / terminais é possível ter um método limpaConsola efectivo.
	// Prém, a implementação actual prejudica pouco a experiência de visualização.
	public void animaMundo(int n, int[] regra, double atrasoEmSegundos) {
		int i; // contador de iterações
		limpaConsola();
		mostraMundo();
		for (i = 0; i < n; i++) {
			atrasa(atrasoEmSegundos);
			limpaConsola();
			iteraMundo(regra);
			mostraMundo();
		}	
	}
	  
    // limpa a consola, preparando-a para nova escrita
  	// implementação provisória; NÃO FAZ O PRETENDIDO;
    // apenas deixa 2 linhas de intervalo entre frames, o que facilita a visualização da animação
    public final static void limpaConsola() {
    	System.out.println("\n");  // 2 linhas de intervalo
    }

	// atrasa (i.e., empata) a execução desta thread
	public final static void atrasa(double segundos) {
		try {
			Thread.sleep((long)(segundos * 1000));
	    }
	    catch(InterruptedException ex) {
	        Thread.currentThread().interrupt();
	    }
	}

}
