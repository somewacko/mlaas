package edu.jhu.bdslss.baft.runner;

import edu.jhu.bdslss.baft.BptiUsedExample;

/**
 * Created by Aileme on 12/6/15.
 */
public class UsedModelRunner {
    public static void main(String[]  args) throws Exception{
        /* args:
                0: Data file path
                1: Final model path
                2: Final stats path
         */
        /*
            Call the spark code(BptiSparkExample) passing the args
         */
        //BptiUsedSparkExample.main(args);
        BptiUsedExample.main(args);
    }
}
