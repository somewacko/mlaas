package edu.jhu.bdslss.baft.runner;

import edu.jhu.bdslss.baft.BptiExample;

/**
 * Created by Aileme on 12/6/15.
 */
public class NewModelRunner {
    public static void main(String[]  args) throws Exception{
        /* args:
                0: Data file path
                1: Final model path
                2: Final stats path
         */
        /*
            Call the spark code(BptiSparkExample) passing the args
         */
        //BptiSparkExample.main(args);
        BptiExample.main(args);
    }
}
