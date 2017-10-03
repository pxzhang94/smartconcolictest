package sunjun.sutd.smartconcolic.algorithm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.random.MersenneTwister;

public class test {
	Set<Integer> unvisited = new HashSet<Integer>();
	RealMatrix coverage_matrix;
	
	public test(){

		unvisited.add(1);
		unvisited.add(2);
		unvisited.add(3);
		coverage_matrix = new OpenMapRealMatrix(8,8);
	}
	
	private void remove() {
		unvisited.remove(2);
	}
	
	public static void add(int x){
		x++;
	}
	public static void main(String[] args) throws IOException{
		double x = 0.9999999999999999999999999999999999999999979;
//		add(x);
//		System.out.println(String.valueOf(x) < "1.0");
//        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,
//                new ArrayBlockingQueue<Runnable>(5));
//         
//        for(int i=0;i<15;i++){
//            MyTask myTask = new MyTask(i);
//            executor.execute(myTask);
//            System.out.println("线程池中线程数目："+executor.getPoolSize()+"，队列中等待执行的任务数目："+
//            executor.getQueue().size()+"，已执行玩别的任务数目："+executor.getCompletedTaskCount());
//        }
//        executor.shutdown();
//
//		Integer[] a = new Integer[4];
//		for(int i = 0; i < a.length; i++)
//			a[i] = i;
		double a = -2.0;
		long b = (long)a;
		System.out.println(b == a);
		List<Integer> list = new ArrayList<>();
//		Collections.addAll(list, a);
//		a[2] = 5;
////		list.
//		
//		for(int i = 0; i < list.size(); i++)
//			System.out.println(list.get(i));
//		int a = list.get(0);
//		list.remove(0);
//		System.out.println(a);
//		for(int i = 0; i <100; i++)
//		System.out.println(new MersenneTwister().nextInt(2));
		
		
//		List<Integer> list1 = list.subList(3, list.size());
//		int a = list.get(0);
//		a = 10;
//		System.out.println(list);
//		list1.add(10);
//		System.out.println(list);
//		System.out.println(list1);
//		Set<Integer> set1 = new HashSet<>();
//		set1.add(0);
//		set1.add(2);
//		Set<Integer> set2 = set1;
//		set2.add(3);
//		System.out.println(set1);
		
		
//		workMap.remove(key)

	}
}

class MyTask implements Runnable {
    private int taskNum;
    
    public MyTask(int num) {
        this.taskNum = num;
    }
     
    @Override
    public void run() {
        System.out.println("正在执行task "+taskNum);
        try {
            Thread.currentThread().sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("task "+taskNum+"执行完毕");
    }
}


class A {
	List<Integer> bound = new ArrayList<>();
	
	public A(List<Integer> bound) {
		this.bound = bound;
	}
}