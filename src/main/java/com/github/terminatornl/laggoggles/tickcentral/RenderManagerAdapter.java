package com.github.terminatornl.laggoggles.tickcentral;

import com.github.terminatornl.laggoggles.profiler.ProfileManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

import static com.github.terminatornl.laggoggles.profiler.ProfileManager.timingManager;

public class RenderManagerAdapter {

	public static void redirectRenderEntity(RenderManager manager, Entity entityIn, double x, double y, double z, float yaw, float partialTicks, boolean b){
		if(ProfileManager.PROFILER_ENABLED_UPDATE_SAFE){
			long LAGGOGGLES_START = System.nanoTime();
			manager.renderEntity(entityIn, x, y, z, yaw, partialTicks, b);
			timingManager.addGuiEntityTime(entityIn.getUniqueID(), System.nanoTime() - LAGGOGGLES_START);
		}else {
			manager.renderEntity(entityIn, x, y, z, yaw, partialTicks, b);
		}
	}
}
