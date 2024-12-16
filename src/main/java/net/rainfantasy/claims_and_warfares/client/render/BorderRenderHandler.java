package net.rainfantasy.claims_and_warfares.client.render;


import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.rainfantasy.claims_and_warfares.CAWConstants;
import org.joml.Vector2d;
import org.joml.Vector3d;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@EventBusSubscriber(modid = CAWConstants.MODID, value = Dist.CLIENT)
public class BorderRenderHandler {
	
	private static final Set<Border> borders = new CopyOnWriteArraySet<>();
	
	//TODO for testing
	static {
		borders.add(new Border(new Vector2d(0, 0), new Vector2d(100, 100), 0xFFFF00));
	}
	
	@SubscribeEvent
	public static void onRenderLevel(RenderLevelStageEvent event) {
		Vec3 pos = event.getCamera().getPosition();
		Vector3d currentPos = new Vector3d(pos.x, pos.y, pos.z);
		if (event.getStage() == Stage.AFTER_WEATHER) {
			for (Border border : borders) {
				BorderRender.renderBorder(border.minPos, border.maxPos, currentPos, 4, border.color, true);
			}
		}
	}
	
	public static void addBorder(Vector2d minPos, Vector2d maxPos, int color) {
		borders.add(new Border(minPos, maxPos, color));
	}
	
	public static void removeBorder(Vector2d minPos, Vector2d maxPos, int color) {
		borders.remove(new Border(minPos, maxPos, color));
	}
	
	public static void removeBorder(Vector2d minPos, Vector2d maxPos) {
		borders.removeIf(border -> border.minPos.equals(minPos) && border.maxPos.equals(maxPos));
	}
	
	public static void clearBorders() {
		borders.clear();
	}
	
}

class Border {
	
	Vector2d minPos;
	Vector2d maxPos;
	int color;
	
	public Border(Vector2d minPos, Vector2d maxPos, int color) {
		this.minPos = minPos;
		this.maxPos = maxPos;
		this.color = color;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(minPos, maxPos, color);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Border border = (Border) o;
		return color == border.color && Objects.equals(minPos, border.minPos) && Objects.equals(maxPos, border.maxPos);
	}
}
