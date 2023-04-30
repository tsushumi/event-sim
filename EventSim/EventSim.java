import java.util.*;
import java.io.*;



public class EventSim
{

	final static int queue_limit = 100;
	final static int Busy = 1;
	final static int Idle = 0;
	private static int next_event_type, num_custs_delayed, num_delays_required,num_events, num_in_q, server_status;
    private static double area_num_in_q, area_server_status, mean_interarrival, mean_service, time, time_last_event,total_of_delays;
    private static double [] time_arrival = new double[ queue_limit+1 ];
    private static double [] time_next_event = new double[ 3 ]; 
    private static List<Double> doubles = new ArrayList<Double>();
    private static PrintWriter output; 

    private static void initialize()
    {

    	time = 0.0;
        

    	server_status = Idle;
        num_in_q = 0;
        time_last_event = 0.0;


        num_custs_delayed = 0;
        total_of_delays = 0.0;
        area_num_in_q = 0.0;
        area_server_status = 0.0;
        


        time_next_event[1] = time + expon(mean_interarrival);
        time_next_event[2] = Math.pow(10,30);
    }
    

    private static void timing()
    {
    	int i;
        double min_time_next_event = Math.pow(10,29);

        next_event_type = 0;


        for (i = 1; i <= num_events; ++i) 
        {
        	if ((time_next_event[ i ] < min_time_next_event)) 
        	{
        		min_time_next_event = time_next_event[ i ];
        		next_event_type = i;
        	}	
        }


        if (next_event_type == 0) 
        {

            output.printf("\n%s%f","Event list empty at time: ", time);
            System.exit(1);	
        }


        time = min_time_next_event;
    }


    private static void arrive()
    {
    	double delay;


    	time_next_event[1] = time + expon(mean_interarrival);


    	if (server_status == Busy) 
    	{

    		++num_in_q;


    		if (num_in_q > queue_limit) 
    		{

                output.printf("\n%s","Overflow of the array time_arrival at");
                output.printf("%s%f", " time: ", time );
    			System.exit(2);
    		}


    		time_arrival[num_in_q] = time;
    	}
    	else 
		{


            delay = 0.0;
            total_of_delays += delay;


            ++num_custs_delayed;
            server_status = Busy;


            time_next_event[2] = time + expon(mean_service);
		}
    }

    // Departure event function.
    private static void depart()
    {
    	int i;
        double delay;

        // Check to see whether the queue is empty.
        if (num_in_q == 0) 
        {
        	// The queue is empty so make the server idle and eliminate the departure (service completion) event from consideration.
        	server_status = Idle;
            time_next_event[2] = Math.pow(10,30);
        }
        else
        {
            // The queue is nonempty, so decrement the nUmber of customers in queue.
        	--num_in_q;

        	// Compute the delay of the customer who is beginning service and update the total delay accumulator.
        	delay  = time - time_arrival[1];
            total_of_delays += delay;

            // Increment the number of customers delayed, and schedule departure
            ++num_custs_delayed;
            time_next_event[2] = time + expon(mean_service);

            // Move each customer in queue (if any) up one place.
            for (i = 1; i <= num_in_q; ++i) 
            {
            	time_arrival[i] = time_arrival[i + 1];
            }
        }
    }

    // Update area accumulators fortime-average statistics.
    private static void update_time_avg_stats()
    {
    	double time_since_last_event;

    	// compute time since last event, and update last-event-timemarker.
    	time_since_last_event = time - time_last_event;
    	time_last_event = time;

    	// Update area under number-in-queue function.
    	area_num_in_q += num_in_q * time_since_last_event;

    	// Update area under server-busy indicator function.
    	area_server_status += server_status * time_since_last_event;
    }

    // Exponential variate generation function
    private static double expon(double mean)
    {
    	double u;

    	// Generate a U(0,1) random variate.
    	u = Math.random();

    	// Return an exponential random variate with mean "mean".
    	return -mean*Math.log(u);
    }
    
        // Main Method
    public static void main(String[] args) throws FileNotFoundException, IOException
    {

        FileReader in = new FileReader("infile.txt");
        Scanner read = new Scanner(in);
        while (read.hasNext()) 
        {
            if (read.hasNextInt()) 
            {

                num_delays_required = read.nextInt();
            } 
            else if (read.hasNextDouble()) 
            {

                double double_val = read.nextDouble();
                doubles.add(double_val);
            }    
        }
        read.close();

        mean_interarrival = doubles.get(0);
        mean_service = doubles.get(1);
        

        num_events = 2;


        PrintWriter output = new PrintWriter("outfile.txt");


        output.printf("%s\n\n", "SINGLE-SERVER QUEUING SYSTEM");
        output.printf("%s%.1f%s\n", "Mean interarrival time: ", mean_interarrival, " minutes");
        output.printf("%s%.1f%s\n", "Mean service time: ", mean_service, " minutes");
        output.printf("%s%d\n", "Number of customers: ", num_delays_required);
        
        // Initialize the simulation.
        initialize();


        while (num_custs_delayed < num_delays_required)
        {

        	timing();


        	update_time_avg_stats();


        	switch (next_event_type)
        	{
        		case 1:
                    arrive () ;
                    break;
                case 2:
                    depart();
                    break;
        	}
        }

        output.printf("\n%s%.5f%s\n", "Average delay in queue: ", total_of_delays / num_custs_delayed, " minutes");
        output.printf("%s%.5f\n", "Average number in queue: ", area_num_in_q / time);
        output.printf("%s%.5f\n", "Server utilization: ", area_server_status / time);
        output.printf("%s%.5f", "Time simulation ended: ", time);

        output.close();
    }
} 