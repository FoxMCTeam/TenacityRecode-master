package dev.tenacity.utils.animations.impl.simple;

import dev.tenacity.utils.animations.AnimationUtil;

public class SimpleAnimation {
	
    private float value;
    private long lastMS;
    
    public SimpleAnimation() {
    	this.value = 0.0F;
    	this.lastMS = System.currentTimeMillis();
    }
    
    public SimpleAnimation(float value){
    	this.value = value;
        this.lastMS = System.currentTimeMillis();
    }
    
    public void setAnimation(final float value, double speed){
    	
        final long currentMS = System.currentTimeMillis();
        final long delta = currentMS - this.lastMS;
        this.lastMS = currentMS;
        
        double deltaValue = 0.0;

        if(speed > 28) {
        	speed = 28;
        }
        
        if (speed != 0.0) {
        	deltaValue = Math.abs(value - this.value) * 0.35f / (10.0 / speed);
        }
        
        this.value = AnimationUtil.calculateCompensation(value, this.value, (long) deltaValue, (int) delta);
    }

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}
}