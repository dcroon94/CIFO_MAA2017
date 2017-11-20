package project;

import java.util.Random;

public class GeneticAlgorithm extends SearchMethod {
	protected Problem instance;
	protected Solution[] population;
	protected Solution currentBest;
	protected int populationSize, numberOfGenerations, currentGeneration, tournamentSize;
	protected double mutationProbability, crossoverProbability;
	protected boolean printFlag;
	protected Random r;

	public GeneticAlgorithm() {
		instance = new Problem(Main.NUMBER_OF_TRIANGLES);
		populationSize = Main.POPULATION_SIZE;
		numberOfGenerations = Main.NUMBER_OF_GENERATIONS;
		mutationProbability = Main.MUTATION_PROBABILIY;
		crossoverProbability = Main.CROSSOVER_PROBABILIY;
		tournamentSize = Main.TOURNAMENT_SIZE;
		printFlag = false;
		currentGeneration = 0;
		r = new Random();
	}

	public void run() {
		initialize();
		search();
		Main.addBestSolution(currentBest);
	}

	public void initialize() {
		population = new Solution[populationSize];
		for (int i = 0; i < population.length; i++) {
			population[i] = new Solution(instance);
			population[i].evaluate();
		}
		updateCurrentBest();
		updateInfo();
		currentGeneration++;
	}

	public void updateCurrentBest() {
		currentBest = getBest(population);
	}

	public void search() {
		while (currentGeneration <= numberOfGenerations) {
			Solution[] offsprings = new Solution[populationSize]; // P'
			for (int k = 0; k < population.length; k++) {
				int p1 = tournamentSelection();
				
				if (r.nextDouble() <= crossoverProbability) {
					int [] parents = {p1, tournamentSelection()};
					offsprings[k] = singlePointCrossover(parents);
				}
				else
					offsprings[k] = population[p1];
				
				if (r.nextDouble() <= mutationProbability) 
					offsprings[k] = offsprings[k].applyMutation();
				
				offsprings[k].evaluate();
			}
			population = replacement(offsprings);
			updateCurrentBest();
			updateInfo();
			currentGeneration++;
		}
	}

	// --------------- Selection
	// --- Tournament Selection
	protected int tournamentSelection() {
		int parentIndex = r.nextInt(populationSize);
		for (int i = 0; i < tournamentSize; i++) {
			int temp = r.nextInt(populationSize);
			if (population[temp].getFitness() < population[parentIndex].getFitness()) {
				parentIndex = temp;
			}
		}
		return parentIndex;
	}

	// --------------- Variation
	// --- Single Point Crossover 
	public Solution singlePointCrossover(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		int crossoverPoint = r.nextInt(instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE);
		for (int i = crossoverPoint; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			offspring.setValue(i, secondParent.getValue(i));
		}
		return offspring;
	}

	// --------------- Replacement: P=P'
	// --- Elitism: one elit
	public Solution[] replacement(Solution[] offspring) {
		Solution bestParent = getBest(population);
		Solution bestOffspring = getBest(offspring);
		if (bestOffspring.getFitness() <= bestParent.getFitness()) {
			return offspring;
		} else {
			Solution[] newPopulation = new Solution[population.length];
			newPopulation[0] = bestParent;
			int worstOffspringIndex = getWorstIndex(offspring);
			for (int i = 0; i < newPopulation.length; i++) {
				if (i < worstOffspringIndex) {
					newPopulation[i + 1] = offspring[i];
				} else if (i > worstOffspringIndex) {
					newPopulation[i] = offspring[i];
				}
			}
			return newPopulation;
		}
	}

	// --------------- Auxiliary methods
	// get best solution
	public Solution getBest(Solution[] solutions) {
		Solution best = solutions[0];
		for (int i = 1; i < solutions.length; i++) {
			if (solutions[i].getFitness() < best.getFitness()) {
				best = solutions[i];
			}
		}
		return best;
	}
	// get best worst
	public int getWorstIndex(Solution[] solutions) {
		Solution worst = solutions[0];
		int index = 0;
		for (int i = 1; i < solutions.length; i++) {
			if (solutions[i].getFitness() > worst.getFitness()) {
				worst = solutions[i];
				index = i;
			}
		}
		return index;
	}
	// update output
	public void updateInfo() {
		currentBest.draw();
		series.add(currentGeneration, currentBest.getFitness());
		if (printFlag) {
			System.out.printf("Generation: %d\tFitness: %.1f\n", currentGeneration, currentBest.getFitness());
		}
	}
}