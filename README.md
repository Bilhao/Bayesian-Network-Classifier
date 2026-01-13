# Classificador de Redes de Bayes (BNC)

Este repositório contém a implementação de um classificador baseado em Redes de Bayes, desenvolvido no âmbito da disciplina de **Algoritmos e Modelação Computacional (2025/2026)**.

O projeto utiliza o algoritmo **Greedy Hill Climber** com pontuação **MDL (Minimum Description Length)** para aprender a estrutura da rede a partir de dados. Inclui uma interface gráfica (Swing) para treino e classificação.

## Funcionalidades

* **Aprendizagem de Rede:**
  * Lê conjuntos de dados em formato CSV (ex: Breast Cancer, Diabetes, Hepatitis).
  * Aprende a estrutura da rede maximizando o score MDL.
  * Parâmetros configuráveis: Máximo de pais (até 2 além da classe), número de grafos aleatórios para inicialização e pseudo-contagem  (com otimização opcional).

* **Classificação:**
  * Carrega redes treinadas (`.bn`).
  * Permite a inserção de parâmetros de um paciente para prever a classe e visualizar as probabilidades de confiança.

## Estrutura do Projeto
* `src/`: Código fonte Java (`MainApp`, `TrainingApp`, `ClassificationApp`, `GreedyHillClimber`, `BN`, etc.).
* `DataSets/`: Arquivos CSV com os dados para treino (provenientes do UCI Machine Learning Repository).
* `TrainedBN/`: Pasta destinada a armazenar as redes de Bayes geradas (`.bn`).

## Como Executar

Certifique-se de ter o Java (JDK) instalado.

### 1. Compilar

Navegue até a raiz do projeto e compile os arquivos fonte:

```bash
javac src/*.java
```

### 2. Executar

Inicie o menu principal da aplicação:

```bash
java -cp src MainApp
```

A partir do menu principal, você pode acessar:

1. **Aprendizagem de Rede:** Para treinar novos modelos.
2. **Classificação de Pacientes:** Para usar modelos existentes.

## Detalhes da Implementação

* **Algoritmo:** Greedy Hill Climber com reinício aleatório (Random Restarts) para evitar máximos locais.
* **Score:** MDL (Minimum Description Length) para penalizar estruturas excessivamente complexas (Overfitting).
* **Suavização:** Utiliza pseudo-contagens (tipicamente ) para evitar probabilidades nulas em eventos raros.

---

*Projeto académico - LEBiom*
