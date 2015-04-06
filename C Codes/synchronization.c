/* 
 * file:        synchronization.c
 * description: Study synchronization in OS using sleeping barber problem
 *
 * 
 * 
 */

#include <stdio.h>
#include <stdlib.h>
#include "hw2.h"

/********** YOUR CODE STARTS HERE ******************/

/* Maximum number of customers */
#define MAX_NUM 5

/*
 * Here's how you can initialize global mutex and cond variables
 */
pthread_mutex_t m = PTHREAD_MUTEX_INITIALIZER;

/* condition - barber sleeping */
pthread_cond_t barber_sleep = PTHREAD_COND_INITIALIZER;

/* condition - customer waiting when barber chair busy */
pthread_cond_t waiting_cust = PTHREAD_COND_INITIALIZER;

/* condition - customer will wait on this condition until haircut finishes */
pthread_cond_t hair_cut = PTHREAD_COND_INITIALIZER;

/* condition - barber waiting for next customer to come (explained further below) */
pthread_cond_t next_customer = PTHREAD_COND_INITIALIZER;

/* condition - customer will wait on this condition until barber wakes (explained further below) */
pthread_cond_t wait_barber_wake = PTHREAD_COND_INITIALIZER;

/* condition - customer waiting when barber wake operation is going on (explained further below) */
pthread_cond_t wake_operation = PTHREAD_COND_INITIALIZER;

/* condition - customer waiting when barber sleep operation is going on (explained further below) */
pthread_cond_t sleep_operation = PTHREAD_COND_INITIALIZER;

/* number of customers currently present in shop */
int cust_num = 0;

/* start barber wake process */
int barber_wake_operation = 0;

/* start barber sleep process */
int barber_sleep_operation = 0;

/* Counters for q3 */
/* Ths variable keeps track of 
 * total number of customers 
 * entering in shop for q3*/
int total_customers = 0;

/*
 * Total number of customers leaving 
 * without haircut when shop is full
 */
int leave_without_haircut = 0;

/*
 * total time spent by customer in shop
 */
double total_time_in_shop = 0.0;

/* This vatiable keeps track of total number of customers 
 * coming in shop who don't find shop full
 */
int total_cust_in_shop = 0;
/* Counter to keep track of average
 * number of customers in the shop
 */
void *counter;
/*
 * Counter to calculate 
 * fraction of time someone is 
 * sitting in the barber's chair
 */
void *in_chair_counter;
/* the barber method
 */
void barber(void)
{
    pthread_mutex_lock(&m);
    /* run forever */
    while (1) {
    /* your code here */

    /* Check if number of customers currently present in shop is zero */
	if (cust_num == 0)
	{
	    printf("\nDEBUG: %f barber goes to sleep \n",timestamp());
        /* Barber sleep operation completes here */
        barber_sleep_operation = 0;

        /* See the note on sleep_operation below, let us say it OPERATION_1 */    
        pthread_cond_broadcast(&sleep_operation);

        /* Barber sleeps here */
		pthread_cond_wait(&barber_sleep, &m);

        /* After waking up, inform customer waiting for barber to wake up */
		pthread_cond_signal(&wait_barber_wake);

        /* See the note on wake_up_operation below, let us say it OPERARION_2 */
		pthread_cond_broadcast(&wake_operation);
                
        /* Barber wake up operation finishes here */
		printf("\nDEBUG: %f barber wakes up\n",timestamp());
        barber_wake_operation = 0;
	}
        
	/* give customer the hair cut*/
	sleep_exp(1.2, &m);

    /* haicut is done, signal customer waiting on this condition */
	pthread_cond_signal(&hair_cut);

    /* 
     * wait for current customer to leave and next customer to come 
     * This wait has been introduced in order to enable current customer
     * to leave.(Otherwise, in absence of this wait barber will continue
     * in infinite while loop and will go in haircut again 
     * for same customer just because, customer did not get chance 
     * to leave barber chair.)
     *
     */
     pthread_cond_wait(&next_customer, &m);
    }
    pthread_mutex_unlock(&m);
}

/* the customer method
 */
void customer(int customer_num)
{
    pthread_mutex_lock(&m);

/* 
 *
 * Note1: about barber_wake_operation: 
 *
 * This operation is introduced to maintain the atomicity of the barber wake operation.
 * Let us consider the logical real life sequence: 
 * Scenario1 (1-4):
 * 1. barber sleeping		
 * 2.  			   customer n sends barber wake up signal
 * 3.                      customer n waits for barber to wake up
 * 4. barber wakes up
 *
 * At step 3 customer leaves monitor and now barber thread is competiting with other 
 * customers to acquire monitor. At this point if other customers acquire monitor and enter shop,
 * the tests will fail. Because, when customer 1 comes, he must immediately wake the barber up.
 * (Our previous versions of solution, when tested, failed on this senario. 
 *  Hence we introduced this logic to handle this scenario.)
 *
 * To ensure this, the "barber_wake_operation" (steps 3 and 4) has to be atomic, but at the same time we can not 
 * deny access to customers coming in shop. Hence, we implement the simple solution, that,
 * when "barber_wake_operation" is in progress, just make the customers wait.
 * As soon as berber-wake_up completes, barber will broadcast (perform OPERATION_2) and these threads will become
 * ready_to_run again.
 *
 */

    if (barber_wake_operation == 1)
	{
  		pthread_cond_wait(&wake_operation, &m);
	}

/* 
 *
 * Note2: about barber_sleep_operation: 
 *
 * This operation is introduced to maintain the atomicity of the barber sleep operation.
 * Let us consider the logical real life sequence: 
 * Barber is giving haircut to last customer in shop. (N = 1, where N is number of customers in shop)
 * Hair cut is done and barber signals, customers waiting on hair_cut.
 * Barber starts waiting for next customer and leaves monitor.
 * The customer waiting on hair_cut gets monitor. He decreases N = 0 and leaves monitor. 
 * (Now barber and other customers are contending to get in monitor.)
 * Ideally, now, as N = 0, barber should immediately sleep.
 * This is possible only if barber thread gets in monitor before any other contending customer thread.
 * But if any customer comes before barber, will lead to test failure because, the senario will be
 * (number of customers in shop = 0, barber = awake, new customer tries to enter)
 * This is illeagal senario. Because, question mentions that as soon as N becomes 0, barber must sleep.
 * (Our previous versions of solution, when tested, failed on this senario. 
 * Hence we introduced this logic to handle this scenario.)
 *
 * Legal senario (Scenario2 (1-4)):
 * 1. barber waiting for next customer		
 * 2. last customer gets hair_cut signal, acquires to monitor
 * 3. signals barber for next customer, makes N = 0, leaves shop
 * 4. Barber acquires monitor, checks N = 0, sleeps                                     
 * 
 * At step 3 customer leaves monitor and now barber thread is competiting with other 
 * customers to acquire monitor. At this point if other customers acquire monitor and enter shop,
 * the tests will fail. Because, when N becomes 0, and customer is still awake.
 *
 * To ensure this, the "barber_sleep_operation" (steps 3 and 4) has to be atomic, but at the same time we can not 
 * deny access to customers coming in shop, since there is space available. 
 * Hence, we implement the simple solution, such that,
 * when "barber_sleep_operation" is in progress, just make the customers wait.
 * As soon as barber_sleep_operation completes, barber will broadcast (perform OPERATION_1) and these threads will become
 * ready_to_run again.
 *
 */

    if (barber_sleep_operation == 1)
	{
  		pthread_cond_wait(&sleep_operation, &m);
	}

    /* customer enters shop */   
	printf("\nDEBUG: %f customer %d enters shop\n",timestamp(), customer_num);

    /* Ths variable keeps track of total number of customers entering in shop for q3*/
	total_customers++;

	stat_count_incr(counter);

    /* If there is space in shop, enter shop*/
	if (cust_num < MAX_NUM)
    {
        /* your code here */
        void *timer = stat_timer();
        stat_timer_start(timer);     

        /* This vatiable keeps track of total number of customers 
         * coming in shop who don't find shop full
         */
        total_cust_in_shop++;
	    cust_num ++;
	    
	    if (cust_num == 1)
		{
            /* Signal barber that first customer has arrived */
    		pthread_cond_signal(&barber_sleep);
            /* Start barber wake up operation, as explained in Note1 */    	            
    		barber_wake_operation = 1;
            /* Wait for barber to wake up */
	    	pthread_cond_wait(&wait_barber_wake, &m);
		}
		else
		{
            /* When barber chair is occupied, wait in waiting area */ 
			pthread_cond_wait(&waiting_cust, &m);
			/* Make sure barber is awake */
			pthread_cond_signal(&barber_sleep);
		}
		stat_count_incr(in_chair_counter);
		printf("\nDEBUG: %f customer %d starts haircut\n",timestamp(), customer_num);

        /* Hair cut in progress */
		pthread_cond_wait(&hair_cut, &m);

        /* Hair cut done, signal customer in waiting area */
	    pthread_cond_signal(&waiting_cust);
            
        /* Decrease number of customers by 1 */       
  	    cust_num--;

		if (cust_num == 0)
    	{   
            /* (Refer Scenario2)
             * Start barber sleep operation,
             * because there are no more customers in shop
             */
			barber_sleep_operation = 1;
    	}
    	printf("\nDEBUG: %f customer %d leaves shop\n",timestamp(), customer_num);
        /* Signal barber that new customer will come now (so that barber will come out of wait)*/	       
    	pthread_cond_signal(&next_customer);

		stat_count_decr(in_chair_counter);
        stat_timer_stop(timer);
        double val = stat_timer_mean(timer);
  	    total_time_in_shop = total_time_in_shop + val;
    }
    else
    {
        /* Leave shop without haircut */
		leave_without_haircut++;
        printf("\nDEBUG: %f customer %d leaves shop\n",timestamp(), customer_num);
    }
    	 
    stat_count_decr(counter);
    pthread_mutex_unlock(&m);
}

/* Threads which call these methods. Note that the pthread create
 * function allows you to pass a single void* pointer value to each
 * thread you create; we actually pass an integer (the customer number)
 * as that argument instead, using a "cast" to pretend it's a pointer.
 */

/* the customer thread function - create 10 threads, each of which calls
 * this function with its customer number 0..9
 */
void *customer_thread(void *context) 
{
    int customer_num = (int)context; 

    /* your code goes here */

    /* customers keep on visiting shop*/
    while(1)
    {
	customer(customer_num);
	sleep_exp(10, NULL);
    }
    
    return 0;
}

/*  barber thread
 */
void *barber_thread(void *context)
{
    /* never returns */
    barber(); 
    return 0;
}

void q2(void)
{
    /* to create a thread:
        pthread_t t; 
        pthread_create(&t, NULL, function, argument);
       note that the value of 't' won't be used in this homework
    */

    /* your code goes here */

	
	pthread_t bThread;
	int i = 0;
	int ret;
        /* create barber thread */
	ret = pthread_create(&bThread, NULL, barber_thread, (void *)i);
	if (ret)
	{
	       printf("Thread creation failed %d\n", ret);
        }
        /* Just to make sure initially, barber comes and sleeps until customers come in shop*/
        sleep_exp(1, NULL);

        /* Customer threads */
	pthread_t customer_threads[10];
	for (i = 0; i < 10; i++)
	{
		//printf("\n reached here to create customers\n");
		ret = pthread_create(&customer_threads[i], NULL, customer_thread, (void *)i);
		if (ret)
		{
		       printf("Thread creation failed %d\n", ret);
	        }
	}
        
	wait_until_done();

	/* Last thing that main() should do */
	//pthread_exit(NULL);    
}

/* For question 3 you need to measure the following statistics:
 *
 * 1. fraction of  customer visits result in turning away due to a full shop 
 *    (calculate this one yourself - count total customers, those turned away)
 * 2. average time spent in the shop (including haircut) by a customer 
 *     *** who does not find a full shop ***. (timer)
 * 3. average number of customers in the shop (counter)
 * 4. fraction of time someone is sitting in the barber's chair (counter)
 *
 * The stat_* functions (counter, timer) are described in the PDF. 
 */

void q3(void)
{
    /* your code goes here */
        counter = stat_counter();
        in_chair_counter = stat_counter();

    /* run simulation */
    q2();

    /*
     * 1. fraction of  customer visits result in turning away due to a full shop 
     */
    float shop_full = 0.0;
    shop_full = leave_without_haircut * (1.0/total_customers);    
    printf("\n1. Fraction of customer visits resulting in turning away = %f",shop_full);

    /*
     * 2. average time spent in the shop (including haircut) by a customer 
     */
    float average_time = 0.0;
    average_time = total_time_in_shop/(total_cust_in_shop * 1.0);    
    printf("\n2. Average time spent in the shop by a customer who does not find a full shop = %f", average_time);

    /*
     * 3. average number of customers in the shop (counter)
     */
    double average_counter = stat_count_mean(counter);
    printf("\n3. Average number of customers in the shop = %f", average_counter);

    /*
     * 4. fraction of time someone is sitting in the barber's chair (counter)
     */
    double average_time_in_barber_chair = stat_count_mean(in_chair_counter);    
    printf("\n4. Average_time_in_barber_chair = %f\n", average_time_in_barber_chair);
}
