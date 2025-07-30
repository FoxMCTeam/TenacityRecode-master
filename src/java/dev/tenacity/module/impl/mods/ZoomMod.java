package dev.tenacity.module.impl.mods;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.game.TickEvent;
import dev.tenacity.event.impl.render.ScrollMouseEvent;
import dev.tenacity.event.impl.render.ZoomFovEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.impl.BooleanSetting;
import dev.tenacity.module.settings.impl.KeybindSetting;
import dev.tenacity.module.settings.impl.NumberSetting;
import dev.tenacity.utils.animations.impl.simple.SimpleAnimation;
import org.lwjglx.input.Keyboard;

//skid shit from soar client
public class ZoomMod extends Module {

	private final SimpleAnimation zoomAnimation = new SimpleAnimation();
	
	private boolean active;
	private float lastSensitivity;
	private float currentFactor = 1;
	
	public boolean wasCinematic;
	
	private final BooleanSetting scrollSetting = new BooleanSetting("Scroll Setting", false);
	private final BooleanSetting smoothZoomSetting = new BooleanSetting("Smooth Zoom Setting",  false);
	private final NumberSetting zoomSpeedSetting = new NumberSetting("Zoom Speed Setting", 14, 20, 5, 1);
	private final NumberSetting factorSetting = new NumberSetting("Factor Setting", 4, 15, 2, 1);
	private final BooleanSetting smoothCameraSetting = new BooleanSetting("Smooth Camera", true);
	private final KeybindSetting keybindSetting = new KeybindSetting(Keyboard.KEY_C);
	
	public ZoomMod() {
		super("module.render.ZoomMod", Category.MODS, "maybe work");
		addSettings(scrollSetting, smoothZoomSetting, zoomSpeedSetting, factorSetting, smoothCameraSetting, keybindSetting);
	}

	@EventTarget
	public void onTick(TickEvent event) {
		if(Keyboard.isKeyDown(keybindSetting.get())) {
			if(!active) {
				active = true;
				lastSensitivity = mc.gameSettings.mouseSensitivity;
				resetFactor();
				wasCinematic = this.mc.gameSettings.smoothCamera;
				mc.gameSettings.smoothCamera = smoothCameraSetting.get();
				mc.renderGlobal.setDisplayListEntitiesDirty();
			}
		}else if(active) {
			active = false;
			setFactor(1);
			mc.gameSettings.mouseSensitivity = lastSensitivity;
			mc.gameSettings.smoothCamera = wasCinematic;
		}
	}
	
	@EventTarget
	public void onZoomFovEvent(ZoomFovEvent event) {
		
		zoomAnimation.setAnimation(currentFactor, zoomSpeedSetting.get().floatValue());

		event.setFov(event.getFov() * (smoothZoomSetting.get() ? zoomAnimation.getValue() : currentFactor));
	}
	
	@EventTarget
	public void onScrollEvent(ScrollMouseEvent event) {
		if(active && scrollSetting.get()) {
			event.setCancelled(true);
			if(event.getAmount() < 0) {
				if(currentFactor < 0.98F) {
					currentFactor+=0.03F;
				}
			}else if(event.getAmount() > 0) {
				if(currentFactor > 0.06F) {
					currentFactor-=0.03F;
				}
			}
		}
	}
	
	public void resetFactor() {
		setFactor(1 / factorSetting.get().floatValue());
	}

	public void setFactor(float factor) {
		if(factor != currentFactor) {
			mc.renderGlobal.setDisplayListEntitiesDirty();
		}
		currentFactor = factor;
	}
}