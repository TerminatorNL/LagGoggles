package cf.terminator.laggoggles.client.gui;

import cf.terminator.laggoggles.packet.ObjectData;
import cf.terminator.laggoggles.profiler.ProfileResult;
import cf.terminator.laggoggles.profiler.ScanType;
import cf.terminator.laggoggles.util.Calculations;
import cf.terminator.laggoggles.util.Graphical;
import cf.terminator.laggoggles.util.RunInClientThread;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class LagOverlayGui {

    private final Minecraft MINECRAFT;
    private final RenderManager RENDER_MANAGER;
    private final FontRenderer FONT_RENDERER;
    private final HashMap<BlockPos, String> BLOCKS_NANO = new HashMap<>();
    private final HashMap<BlockPos, Double> BLOCKS_HEAT = new HashMap<>();
    private final HashMap<Entity,   String> ENTITY_NANO = new HashMap<>();
    private final HashMap<Entity,   Double> ENTITY_HEAT = new HashMap<>();
    private final ProfileResult result;
    private AtomicBoolean isShowing = new AtomicBoolean(false);

    private double flash = 1;
    private boolean flashIncreasing = false;

    private final HashMap<ChunkPos, Double> CHUNKS = new HashMap<>();
    private final ScanType type;

    private static AtomicReference<LagOverlayGui> INSTANCE = new AtomicReference<>();
    private final QuickText quickText;


    public static void create(ProfileResult data){
        if(INSTANCE.get() != null){
            INSTANCE.get()._hide();
        }
        INSTANCE.set(new LagOverlayGui(data));
    }

    public static void destroy(){
        if(INSTANCE.get() != null){
            INSTANCE.get()._hide();
            INSTANCE.set(null);
        }
    }

    public static void show(){
        if(INSTANCE.get() != null && INSTANCE.get().isShowing.get() == false){
            INSTANCE.get()._show();
        }
    }

    public static void hide(){
        if(INSTANCE.get() != null && INSTANCE.get().isShowing.get() == true){
            INSTANCE.get()._hide();
        }
    }

    public static boolean isShowing(){
        return INSTANCE.get() != null && INSTANCE.get().isShowing.get();
    }

    private LagOverlayGui(ProfileResult data){
        this.result = data;
        this.type = result.getType();
        MINECRAFT = Minecraft.getMinecraft();
        RENDER_MANAGER = MINECRAFT.getRenderManager();
        FONT_RENDERER = MINECRAFT.fontRenderer;
        this.quickText = new QuickText(result.getType().getText(result));
        scanAndAddEntities();
    }

    private void calculateFlash(){
        if(flashIncreasing){
            flash = flash + 0.005D;
            if (flash >= 1){
                flashIncreasing = false;
            }
        }else{
            flash = flash - 0.005D;
            if(flash <= 0.125){
                flashIncreasing = true;
            }
        }
    }

    private synchronized void scanAndAddEntities(){
        BLOCKS_NANO.clear();
        BLOCKS_HEAT.clear();
        ENTITY_NANO.clear();
        ENTITY_HEAT.clear();
        CHUNKS.clear();
        if(MINECRAFT.isGamePaused() || MINECRAFT.world == null || MINECRAFT.world.loadedEntityList == null){
            return;
        }
        if(type == ScanType.WORLD){

            for(ObjectData objectData : result.getData()){
                long nanos = objectData.getValue(ObjectData.Entry.NANOS);
                switch (objectData.type){
                    case TILE_ENTITY:
                    case BLOCK:
                        if((int) objectData.getValue(ObjectData.Entry.WORLD_ID) != MINECRAFT.world.provider.getDimension()){
                            continue;
                        }
                        BlockPos pos = new BlockPos(objectData.getValue(ObjectData.Entry.BLOCK_POS_X), objectData.getValue(ObjectData.Entry.BLOCK_POS_Y), objectData.getValue(ObjectData.Entry.BLOCK_POS_Z));
                        if(MINECRAFT.player.getDistanceSq(pos) > 36864){
                            /* More than 12 chunks away, we don't draw. */
                            continue;
                        }
                        double heat = Calculations.heat(nanos, result);
                        BLOCKS_HEAT.put(pos,heat);
                        BLOCKS_NANO.put(pos,Calculations.muPerTickString(nanos, result));

                        ChunkPos c = new ChunkPos(pos);
                        if(CHUNKS.containsKey(c) == false){
                            CHUNKS.put(c, heat);
                        }else{
                            CHUNKS.put(c, heat + CHUNKS.get(c));
                        }
                        if( heat < 0 && CHUNKS.get(c) == 0){
                            CHUNKS.remove(c);
                        }

                        break;
                    case ENTITY:
                        if((int) objectData.getValue(ObjectData.Entry.WORLD_ID) != MINECRAFT.world.provider.getDimension()){
                            continue;
                        }
                        UUID entityID = objectData.getValue(ObjectData.Entry.ENTITY_UUID);
                        for(Entity entity : new ArrayList<>(MINECRAFT.world.loadedEntityList)){
                            if(entity.getPersistentID().equals(entityID)){
                                if(entity == MINECRAFT.player){
                                    continue;
                                }
                                ENTITY_NANO.put(entity, Calculations.muPerTickString(nanos, result));
                                ENTITY_HEAT.put(entity, Calculations.heat(nanos, result));
                                break;
                            }
                        }
                        break;
                }
            }

        }else{

            for (ObjectData objectData : result.getData()) {
                long nanos = objectData.getValue(ObjectData.Entry.NANOS);
                switch (objectData.type) {
                    case GUI_BLOCK:
                        BlockPos pos = new BlockPos(objectData.getValue(ObjectData.Entry.BLOCK_POS_X), objectData.getValue(ObjectData.Entry.BLOCK_POS_Y), objectData.getValue(ObjectData.Entry.BLOCK_POS_Z));
                        BLOCKS_HEAT.put(pos, Calculations.heatNF(nanos, result));
                        BLOCKS_NANO.put(pos, Calculations.NFString(nanos, result.getTotalFrames()));
                        break;
                    case GUI_ENTITY:
                        UUID entityID = objectData.getValue(ObjectData.Entry.ENTITY_UUID);
                        for (Entity entity : new ArrayList<>(MINECRAFT.world.loadedEntityList)) {
                            if (entity.getPersistentID().equals(entityID)) {
                                if (entity == MINECRAFT.player) {
                                    continue;
                                }
                                ENTITY_HEAT.put(entity, Calculations.heatNF(nanos, result));
                                ENTITY_NANO.put(entity, Calculations.NFString(nanos, result.getTotalFrames()));
                                break;
                            }
                        }

                }
            }

        }
    }

    private void mark(int x, int y, int z, double heat){

        double[] c = Graphical.heatToColor(heat);

        GL11.glColor4d(c[0],c[1],c[2],Math.min(heat/100D, flash));

        /* NORTH */
        GL11.glVertex3d(x,      y + 1,  z       );
        GL11.glVertex3d(x + 1,  y + 1,  z       );
        GL11.glVertex3d(x + 1,  y,      z       );
        GL11.glVertex3d(x,      y,      z       );

        /* EAST */
        GL11.glVertex3d(x + 1,  y,      z + 1   );
        GL11.glVertex3d(x + 1,  y,      z       );
        GL11.glVertex3d(x + 1,  y + 1,  z       );
        GL11.glVertex3d(x + 1,  y + 1,  z + 1   );

        /* SOUTH */
        GL11.glVertex3d(x + 1,  y + 1,  z + 1   );
        GL11.glVertex3d(x    ,  y + 1,  z + 1   );
        GL11.glVertex3d(x    ,  y    ,  z + 1   );
        GL11.glVertex3d(x + 1,  y    ,  z + 1   );

        /* WEST */
        GL11.glVertex3d(x,      y + 1,  z + 1   );
        GL11.glVertex3d(x,      y + 1,  z       );
        GL11.glVertex3d(x,      y,      z       );
        GL11.glVertex3d(x,      y,      z + 1   );

        /* TOP */
        GL11.glVertex3d(x,      y + 1,  z       );
        GL11.glVertex3d(x,      y + 1,  z + 1   );
        GL11.glVertex3d(x + 1,  y + 1,  z + 1   );
        GL11.glVertex3d(x + 1,  y + 1,  z       );

        /* BOTTOM */
        GL11.glVertex3d(x + 1,  y    ,  z       );
        GL11.glVertex3d(x + 1,  y    ,  z + 1   );
        GL11.glVertex3d(x,      y    ,  z + 1   );
        GL11.glVertex3d(x,      y    ,  z       );
    }

    private void drawChunk(ChunkPos chunk, Double heat, double pY){


        double[] c = Graphical.heatToColor(heat / 10);

        GL11.glColor4d(c[0],c[1],c[2],0.4);

        int xStart = chunk.getXStart();
        int zStart = chunk.getZStart();
        int xEnd = chunk.getXEnd() + 1;
        int zEnd = chunk.getZEnd() + 1;

        GL11.glVertex3d(xEnd,   pY + 80 ,  zStart );
        GL11.glVertex3d(xEnd,   pY + 80 ,  zEnd   );
        GL11.glVertex3d(xStart, pY + 80 ,  zEnd   );
        GL11.glVertex3d(xStart, pY + 80 ,  zStart );


    }


    @SubscribeEvent
    public void onDraw(RenderWorldLastEvent event){
        float partialTicks = event.getPartialTicks();
        double pX = MINECRAFT.player.prevPosX + (MINECRAFT.player.posX - MINECRAFT.player.prevPosX) * partialTicks;
        double pY = MINECRAFT.player.prevPosY + (MINECRAFT.player.posY - MINECRAFT.player.prevPosY) * partialTicks;
        double pZ = MINECRAFT.player.prevPosZ + (MINECRAFT.player.posZ - MINECRAFT.player.prevPosZ) * partialTicks;

        /* Prepare */
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        GL11.glTranslated(-pX,-pY,-pZ);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glHint( GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST );


        calculateFlash();
        drawBlocksAndChunks(pY);
        drawEntities(partialTicks);
        drawTileEntityTags();
        drawEntityTags(partialTicks);



        /* Restore settings */
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
        GL11.glPopAttrib();

    }

    private void highLightMob(Entity entity, double heat, float partialTicks){

        double[] c = Graphical.heatToColor(heat);

        GL11.glColor4d(c[0],c[1],c[2],0.6);


        double pX;
        double pY;
        double pZ;

        if(entity.isDead) {
            pX = entity.posX;
            pY = entity.posY;
            pZ = entity.posZ;
        }else{
            pX = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
            pY = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks;
            pZ = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;
        }

        GL11.glVertex3d(pX, pY ,        pZ);
        GL11.glVertex3d(pX, pY + 2.3 ,  pZ);
    }

    private void drawBlocksAndChunks(double pY){
        /* Draw blocks! */
        GL11.glBegin(GL11.GL_QUADS);
        for(Map.Entry<BlockPos, Double> e : BLOCKS_HEAT.entrySet()){
            if(e.getValue() > 1) {
                mark(e.getKey().getX(), e.getKey().getY(), e.getKey().getZ(), e.getValue());
            }
        }

        /* Draw chunks! */
        for(Map.Entry<ChunkPos, Double> e : CHUNKS.entrySet()) {
            drawChunk(e.getKey(), e.getValue(), pY);
        }
        GL11.glEnd();
    }

    private void drawEntities(float partialTicks){
        GL11.glBegin(GL11.GL_LINES);
        for(Map.Entry<Entity, Double> e : ENTITY_HEAT.entrySet()) {
            highLightMob(e.getKey(), e.getValue(), partialTicks);
        }
        GL11.glEnd();
    }

    private void drawTileEntityTags(){
        /* TILE ENTITIES */
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        for(Map.Entry<BlockPos, String> e : BLOCKS_NANO.entrySet()){
            GL11.glPushMatrix();
            double offX = (e.getKey().getX() + 0.5D);
            double offY = (e.getKey().getY() + 0.5D);
            double offZ = (e.getKey().getZ() + 0.5D);

            GL11.glTranslated(offX, offY, offZ);
            GL11.glRotated(-RENDER_MANAGER.playerViewY, 0.0, 1.0, 0.0);
            GL11.glRotated(RENDER_MANAGER.playerViewX, 1.0, 0.0, 0.0);
            /* Flip it! */
            GL11.glScaled(-0.015, -0.015, 0.015);


            FONT_RENDERER.drawString(e.getValue(), -FONT_RENDERER.getStringWidth(e.getValue()) / 2, 0, 0xCCCCCC /* GRAY */);

            GL11.glPopMatrix();
        }
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    private synchronized void drawEntityTags(float partialTicks){
        /* ENTITIES */
        for(Map.Entry<Entity, String> e : ENTITY_NANO.entrySet()){
            GL11.glPushMatrix();

            Entity entity = e.getKey();
            double pXe;
            double pYe;
            double pZe;

            if(entity.isDead) {
                pXe = entity.posX;
                pYe = entity.posY;
                pZe = entity.posZ;
            }else{
                pXe = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
                pYe = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks;
                pZe = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;
            }

            GL11.glTranslated(pXe, pYe + 2.3D, pZe);
            GL11.glRotated(-RENDER_MANAGER.playerViewY, 0.0, 1.0, 0.0);
            GL11.glRotated(RENDER_MANAGER.playerViewX, 1.0, 0.0, 0.0);
            /* Flip it! */
            GL11.glScaled(-0.015, -0.015, 0.015);


            double[] c = Graphical.heatToColor(ENTITY_HEAT.get(entity));
            GL11.glColor4d(c[0],c[1],c[2], 0.6);


            GL11.glBegin(GL11.GL_QUADS);
            String text = e.getValue();
            if(entity.isDead){
                text = text + " (" + entity.getName() + ")";
            }
            int width_plus_2 = FONT_RENDERER.getStringWidth(text) + 2;
            int height_div_2_plus_1 = (FONT_RENDERER.FONT_HEIGHT/2) + 1;
            GL11.glVertex3d(0           ,-height_div_2_plus_1,0);
            GL11.glVertex3d(0           , height_div_2_plus_1 - 1,0);
            GL11.glVertex3d(width_plus_2, height_div_2_plus_1 - 1,0);
            GL11.glVertex3d(width_plus_2,-height_div_2_plus_1,0);
            GL11.glEnd();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4d(0,0,0,1);

            FONT_RENDERER.drawString(text, 1, -FONT_RENDERER.FONT_HEIGHT/2, 0x000000);

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glPopMatrix();
        }
    }

    private void _hide(){
        MinecraftForge.EVENT_BUS.unregister(this);
        quickText.hide();
        isShowing.set(false);
    }

    private void _show(){
        isShowing.set(true);
        new Thread(TIMELY_UPDATES).start();
        MinecraftForge.EVENT_BUS.register(this);
        quickText.show();
    }

    private Runnable TIMELY_UPDATES = new Runnable() {
        @Override
        public void run() {
            while(isShowing.get()){
                try {
                    Thread.sleep(1000);
                    new RunInClientThread(new Runnable() {
                        @Override
                        public void run() {
                            if(isShowing.get()) {
                                scanAndAddEntities();
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
