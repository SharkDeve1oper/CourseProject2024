package org.shcherbakov;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ResourceAllocator {


    @Setter
    private int resources;

    @Setter
    private int enterprises;

    @Setter
    private double[][] profit;

    private final List<int[]> solutions = new ArrayList<>();

    private double maxProfit = 0;

    public ResourceAllocator(double[][] profit) {
        this.resources = profit.length;
        this.enterprises = profit[0].length;
        this.profit = profit;
    }

    public void findMaxProfit() {
        solutions.clear();
        maxProfit = 0;
        int[] allocation = new int[enterprises];
        findMaxProfit(allocation, 0, resources, 0);
    }

    private void findMaxProfit(int[] allocation, int enterpriseIndex, int remainingResources, double currentProfit) {
        if (enterpriseIndex == enterprises) {
            if (currentProfit > maxProfit) {
                maxProfit = currentProfit;
                solutions.clear();
            }
            if (currentProfit == maxProfit) {
                solutions.add(allocation.clone());
            }
            return;
        }

        for (int i = 0; i <= remainingResources; i++) {
            allocation[enterpriseIndex] = i;
            double profitForResources = (i > 0) ? profit[i - 1][enterpriseIndex] : 0;
            findMaxProfit(allocation, enterpriseIndex + 1, remainingResources - i, currentProfit + profitForResources);
        }
    }

    // Метод для удобного вывода распределений
    public static void printAllocation(int[] allocation) {
        for (int i = 0; i < allocation.length; i++) {
            System.out.println("Предприятию " + (i + 1) + " выделено " + allocation[i] + " ресурсов.");
        }
        System.out.println();
    }
}
