package coursework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import model.Fitness;
import model.Individual;
import model.LunarParameters.DataSet;
import model.NeuralNetwork;

/**
 * Implements a basic Evolutionary Algorithm to train a Neural Network
 * 
 * You Can Use This Class to implement your EA or implement your own class that
 * extends {@link NeuralNetwork}
 * 
 */
public class ExampleEvolutionaryAlgorithm extends NeuralNetwork
{

	/**
	 * The Main Evolutionary Loop
	 */
	@Override
	public void run()
	{
		// Initialise a population of Individuals with random weights
		population = initialise();

		// Record a copy of the best Individual in the population
		best = getBest();
		System.out.println("Best From Initialisation " + best);

		/**
		 * main EA processing loop
		 */

		while (evaluations < Parameters.maxEvaluations)
		{
			int n_children = (int) (Parameters.popSize * Parameters.replacement_rate);
			if (n_children < 1)
				n_children = 1;
			ArrayList<Individual> children = new ArrayList<Individual>();

			for (int i = 0; i < n_children; i++)
			{
				// Selection
				//Individual parent1 = Parameters.operator.equals("ranked") ? select_ranked() : select_tournament(2);
				//Individual parent2 = select_tournament(2);
				//Individual parent2 = Parameters.operator.equals("ranked") ? select_ranked() : select_tournament(2);
				
				Individual parent1 = new Individual();
				Individual parent2 = new Individual();
				if (Parameters.operator.equals("ranked"))
				{
					parent1 = select_ranked();
					parent2 = select_ranked();
				}
				else if (Parameters.operator.equals("tournament"))
				{
					parent1 = select_tournament(2);
					parent2 = select_tournament(2);
				}
				else if (Parameters.operator.equals("ranked/tournament"))
				{
					parent1 = select_ranked();
					parent2 = select_tournament(2);
				}
				else if (Parameters.operator.equals("ranked/random"))
				{
					parent1 = select_ranked();
					parent2 = select_random();
				}
				else if (Parameters.operator.equals("tournament/random"))
				{
					parent1 = select_ranked();
					parent2 = select_random();
				}
				else if (Parameters.operator.equals("random"))
				{
					parent1 = select_random();
					parent2 = select_random();
				}
				

				// Crossover
				children.add(k_point_crossover(parent1, parent2, Parameters.cross_k));
			}

			// Mutation
			mutate(children);

			// Evaluation
			evaluateIndividuals(children);

			replace(children);

			// Get best individual
			best = getBest();

			// Does something that the jar won't let me see. May be not important
			// setChanged();
			// notifyObservers(best.copy());
			outputStats();
		}

		// save the trained network to disk
		saveNeuralNetwork();
	}



	// Produces a list of children by crossover of 2 parents
	private ArrayList<Individual> reproduce(Individual parent1, Individual parent2)
	{
		ArrayList<Individual> children = new ArrayList<>();
		children.add(k_point_crossover(parent1, parent2, 3));
		children.add(k_point_crossover(parent2, parent1, 3));
		return children;
	}



	// Tournament selection. Uses n members chosen randomly.
	private Individual select_tournament(int n)
	{
		ArrayList<Individual> parents = new ArrayList<Individual>();
		for (int i = 0; i < n; i++)
			parents.add(select_random());

		double best_fitness = 99.0;
		int best = 0;
		for (int i = 1; i < parents.size(); i++)
			if (parents.get(i).fitness < best_fitness)
			{
				best_fitness = parents.get(i).fitness;
				best = i;
			}
		return parents.get(best).copy();
	}
	
	
	
	// Ranked selection
	private Individual select_ranked()
	{
		int t = 0;
		for (int i = 0; i < Parameters.popSize; i++)
			t += i;
		int ran = Parameters.random.nextInt(t) + 1;
		ArrayList<Individual> parents = new ArrayList<Individual>();
		Individual result = new Individual();
		parents.addAll(population);
		for (int i = 1; i <= Parameters.popSize; i++)
		{
			int worst = getWorstIndex(parents);
			if (ran >= t)
				result = parents.get(worst).copy();
			else
			{
				t -= i;
				parents.remove(worst);
			}
		}
		return result;
	}



	// Random parent
	private Individual select_random()
	{
		Individual parent = population.get(Parameters.random.nextInt(Parameters.popSize));
		return parent.copy();
	}



	// K point crossover for 2 given parents. If k is more than n of genes returns
	// parent1
	private Individual k_point_crossover(Individual parent1, Individual parent2, Integer k)
	{
		if (Parameters.getNumGenes() < k)
		{
			Individual child = new Individual();

			ArrayList<Integer> c_points = new ArrayList<Integer>();
			while (c_points.size() < k)
			{
				Integer point = Parameters.random.nextInt(k - 1) + 1;
				if (c_points.contains(point))
					continue;
				c_points.add(point);
			}

			boolean p1 = Parameters.random.nextBoolean();
			for (int i = 0; i < Parameters.getNumGenes(); i++)
			{
				if (c_points.contains(i))
					p1 = !p1;

				if (p1)
					child.chromosome[i] = parent1.chromosome[i];
				else
					child.chromosome[i] = parent2.chromosome[i];
			}
			return child;
		} else
			return parent1.copy();
	}



	// Uniform crossover
	private Individual uniform_crossover(Individual parent1, Individual parent2)
	{
		Individual child = new Individual();
		for (int i = 0; i < child.chromosome.length; i++)
		{
			if (Parameters.random.nextBoolean())
				child.chromosome[i] = parent1.chromosome[i];
			else
				child.chromosome[i] = parent2.chromosome[i];
		}
		return child;
	}



	// Mutates an individual
	private Individual forced_mutate(Individual parent)
	{
		Individual child = parent.copy();
		while (Arrays.equals(child.chromosome, parent.chromosome))
		{
			for (int i = 0; i < child.chromosome.length; i++)
			{
				if (Parameters.random.nextDouble() < Parameters.mutateRate)
				{
					if (Parameters.random.nextBoolean())
					{
						child.chromosome[i] += (Parameters.mutateChange);
					} else
					{
						child.chromosome[i] -= (Parameters.mutateChange);
					}
				}
			}
		}
		return child;

	}



//	// Leaky ReLu
//	@Override
//	public double activationFunction(double x)
//	{
//		if (x < 0)
//		{
//			return x * 0.01;
//		} else
//		{
//			return x;
//		}
//	}

	@Override
	public double activationFunction(double x)
	{
		return x;
//		if (x < -20.0)
//		{
//			return -1.0;
//		} else if (x > 20.0)
//		{
//			return 1.0;
//		}
//		return Math.tanh(x);
	}



///////////////////////////////////////////////////////////////////////////////////////////////////////////////	

	/**
	 * Sets the fitness of the individuals passed as parameters (whole population)
	 * 
	 */
	private void evaluateIndividuals(ArrayList<Individual> individuals)
	{
		for (Individual individual : individuals)
		{
			individual.fitness = Fitness.evaluate(individual, this);
		}
	}



	/**
	 * Mutation
	 * 
	 * 
	 */
	private void mutate(ArrayList<Individual> individuals)
	{
		for (Individual individual : individuals)
		{
			for (int i = 0; i < individual.chromosome.length; i++)
			{
				if (Parameters.random.nextDouble() < Parameters.mutateRate)
				{
					if (Parameters.random.nextBoolean())
					{
						individual.chromosome[i] += (Parameters.mutateChange);
					} else
					{
						individual.chromosome[i] -= (Parameters.mutateChange);
					}
				}
			}
		}
	}



	/**
	 * Generates a randomly initialised population
	 * 
	 */
	private ArrayList<Individual> initialise()
	{
		population = new ArrayList<>();
		for (int i = 0; i < Parameters.popSize; ++i)
		{
			// chromosome weights are initialised randomly in the constructor
			Individual individual = new Individual();
			population.add(individual);
		}
		evaluateIndividuals(population);
		return population;
	}



	/**
	 * Returns a copy of the best individual in the population
	 * 
	 */
	private Individual getBest()
	{
		best = null;
		;
		for (Individual individual : population)
		{
			if (best == null)
			{
				best = individual.copy();
			} else if (individual.fitness < best.fitness)
			{
				best = individual.copy();
			}
		}
		return best;
	}



	/**
	 * 
	 * Replaces the worst member of the population (regardless of fitness)
	 * 
	 */
	private void replace(ArrayList<Individual> individuals)
	{
		for (Individual individual : individuals)
		{
			int idx = getWorstIndex();
			population.set(idx, individual);
		}
	}



	/**
	 * Returns the index of the worst member of the population
	 * 
	 * @return
	 */
	private int getWorstIndex()
	{
		Individual worst = null;
		int idx = -1;
		for (int i = 0; i < population.size(); i++)
		{
			Individual individual = population.get(i);
			if (worst == null)
			{
				worst = individual;
				idx = i;
			} else if (individual.fitness > worst.fitness)
			{
				worst = individual;
				idx = i;
			}
		}
		return idx;
	}



	/**
	 * Returns the index of the worst member of the array
	 * 
	 * @return
	 */
	private int getWorstIndex(ArrayList<Individual> individuals)
	{
		Individual worst = null;
		int idx = -1;
		for (int i = 0; i < individuals.size(); i++)
		{
			Individual individual = individuals.get(i);
			if (worst == null)
			{
				worst = individual;
				idx = i;
			} else if (individual.fitness > worst.fitness)
			{
				worst = individual;
				idx = i;
			}
		}
		return idx;
	}
}
