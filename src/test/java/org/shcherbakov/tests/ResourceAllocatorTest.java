package org.shcherbakov.tests;

import org.junit.jupiter.api.Test;
import org.shcherbakov.ResourceAllocator;

import static org.junit.jupiter.api.Assertions.*;

public class ResourceAllocatorTest {

    @Test
    public void testMaxProfitCalculation() {
        double[][] profit = {
                {0.8, 0.6, 0.4, 0.7},
                {1.7, 0.9, 1.2, 0.8},
                {2.0, 1.7, 2.0, 1.2},
                {2.6, 1.9, 2.7, 1.5},
                {2.8, 2.2, 3.6, 1.6},
                {3.6, 2.4, 4.0, 2.2},
                {3.8, 2.7, 4.2, 2.8},
                {4.0, 3.0, 4.2, 3.2},
                {4.3, 3.0, 4.2, 3.2},
                {4.3, 3.0, 4.2, 3.2}
        };
        ResourceAllocator allocator = new ResourceAllocator(profit);
        allocator.findMaxProfit();
        assertEquals(7.0, allocator.getMaxProfit());
        assertEquals(2, allocator.getSolutions().size());

        for (int[] solution : allocator.getSolutions()) {
            double maxProfit = 0;
            int resourceCount = 0;

            maxProfit = solution[0]-1 >= 0 ? maxProfit + profit[solution[0]-1][0] : maxProfit;
            resourceCount += solution[0];

            maxProfit = solution[1]-1 >= 0 ? maxProfit + profit[solution[1]-1][1] : maxProfit;
            resourceCount += solution[1];

            maxProfit = solution[2]-1 >= 0 ? maxProfit + profit[solution[2]-1][2] : maxProfit;
            resourceCount += solution[2];

            maxProfit = solution[3]-1 >= 0 ? maxProfit + profit[solution[3]-1][3] : maxProfit;
            resourceCount += solution[3];

            assertEquals(7.0, maxProfit);
            assertTrue(resourceCount <= 10);
        }
    }
}

