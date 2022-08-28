package com.jovistar.commons.threads;

import java.util.Vector;

import com.jovistar.commons.exception.CCMException;
import com.jovistar.commons.facade.ModelFacade;

public class JobRunner extends Vector implements Runnable {

	private static JobRunner instance;

	protected Thread thread; //this thread
    private Semaphore semaphore;
    private boolean running;
    private boolean ad_job_running = false;//to limit only one instance of 'ad job' running
    private int priority;

    public static JobRunner getInstance(){
    	if(instance==null)
    		instance=new JobRunner(1);
    	return instance;
    }

    private JobRunner(int priority) {
        semaphore = new Semaphore(0);
        this.priority = priority;
    }

    public void run(Runnable r) throws CCMException {
        Runnable job = r;
        try {
            ServiceRequestJob srjob = (ServiceRequestJob) job;
            /*************************************************
             ********** RESTRICT ONLY ONE AD JOB *************
             *************************************************/
            if (srjob.getSvc() == ModelFacade.getInstance().SVC_AD
                    && srjob.getOp() == ModelFacade.getInstance().OP_GET && !ad_job_running) {
//                Log.d("JobRunner","JobRunner:run(), AD job created");
                ad_job_running = true;
            } else if (srjob.getSvc() == ModelFacade.getInstance().SVC_AD
                    && srjob.getOp() == ModelFacade.getInstance().OP_GET && ad_job_running) {
//            	Log.d("JobRunner","JobRunner:run(), AD job suppressed");
                return;
            }
        } catch (ClassCastException e) {
            //this was not a ServiceRequestJob
        }
        //not more than 3 threads can run in this runner
//        if (size() > 1) {
        //          throw new CCMException("busy. please wait");
        //    }
        addElement(job);
        semaphore.release();

        if (thread == null) {
            (thread = new Thread(this)).setPriority(priority);
            thread.start();
        }
    }

    private Runnable getNextJob() {
//        Log.d("JobRunner","JobRunner:getNextJob(), waiting for next job");
        semaphore.acquire();
        if (!isEmpty()) {
            Object o = this.firstElement();
            removeElement(o);
            return (Runnable) o;
        }

        return null;
    }

    public void run() {
        Runnable r = null;
        if (!running) {
            running = true;
            while ((r = getNextJob()) != null) {
//                Log.d("JobRunner","JobRunner:getNextJob(), got next job!! ACTIVE THREADS:" + Thread.activeCount() + " jobrunner size:" + this.size());
//                try {
                    r.run();
//                } catch (Throwable e) {
//                    e.printStackTrace();
//                }
                try {
                    ServiceRequestJob job = (ServiceRequestJob) r;
                    if (job.getSvc() == ModelFacade.getInstance().SVC_AD
                            && job.getOp() == ModelFacade.getInstance().OP_GET) {
                        ad_job_running = false;
//                        Log.d("JobRunner","JobRunner:run(), AD job completed");
                    }
                    job = null;
                } catch (ClassCastException e) {
                    //this was not a ServiceRequestJob
                }
                r = null;
            }
            running = false;
        }
        thread = null;
        //CCMIDlet.debug("JobRunner:run out");
    }
}
