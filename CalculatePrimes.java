import java.util.concurrent.*;
import java.io.*;
import java.util.Date;

public class CalculatePrimes
{
	private static int N = 100000000; // Max number to evaluate
	private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors() * 2;
	private static ConcurrentSkipListSet<Integer> results = new ConcurrentSkipListSet<>();
	
	static class EvaluatePrime extends RecursiveAction
	{
		int from, to; // Range of values to evaluate
		
		public EvaluatePrime(int from, int to)
		{
			this.from = from;
			this.to = to;
		}
		
		public void compute()
		{
			// Decide if the task is small enough to handle in one thread, otherwise fork it
			if ( (to - from) <= N / (NUM_THREADS) )
			{
				for (int i = from; i <= to; i++) // evaluate each number in the range
				{
					if (evaluatePrime(i))
					{
						results.add(new Integer(i)); // check if prime, if it is, add to the results
					}
				}
			}
			else
			{
				int mid = (from + to) / 2;
				invokeAll(new EvaluatePrime(from, mid), new EvaluatePrime(mid + 1, to));
			}
		}
		static boolean evaluatePrime(int candidate)
		{
			if (candidate < 2) // 0, 1, and negative numbers cannot be prime
				return false;
			if (candidate == 2) // 2 is prime
				return true;
			if (candidate % 2 == 0) // if we're even, can't be prime
				return false;
			for (int i = 3; i <= (int)Math.sqrt(candidate); i += 2) // try dividing by all odd ints 3..sqrt(candidate)
			{
				if ( candidate % i == 0) 
				{
					return false; // if the being evaluated is divisible by j, it is not prime, stop trying
				}
			}
			return true; // if we made it this far, we are prime
		}
	}
	
	public static void main(String[] args)
	{
		long startTime = new Date().getTime();
		ForkJoinPool pool = new ForkJoinPool(NUM_THREADS);
		pool.invoke(new EvaluatePrime(0, N));
		
		try (PrintWriter outputFile = new PrintWriter(new FileWriter("primeResults.txt")))
		{			
			outputFile.println(results.toString());
			
			System.out.println("Wrote results to primeResults.txt");
			long endTime = new Date().getTime();
			long elapsed = endTime - startTime;
			System.out.println("Elapsed Time: " + elapsed + "ms");
		}
		catch (IOException e)
		{
			System.out.println("Failed to write primeResults.txt");
			e.printStackTrace();
		}
	}
}