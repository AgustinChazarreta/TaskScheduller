package com.AgsCh.task_scheduler.model;

public enum Category {

    CATEGORY_1(1),
    CATEGORY_2(2),
    CATEGORY_3(3),
    CATEGORY_4(4);

    private final int hierarchy;
    
        Category(int hierarchy) {
            this.hierarchy = hierarchy;
        }
    
        public int hierarchy() {
            return hierarchy;
        }
}