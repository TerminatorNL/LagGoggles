package cf.terminator.laggoggles.client.gui;

import cf.terminator.laggoggles.packet.SPacketScanResult;
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

public class LagOverlayGui {

    private final Minecraft MINECRAFT;
    private final RenderManager RENDER_MANAGER;
    private final FontRenderer FONT_RENDERER;
    private final HashMap<BlockPos, String> BLOCKS_NANO = new HashMap<>();
    private final HashMap<BlockPos, Double> BLOCKS_HEAT = new HashMap<>();
    private final HashMap<Entity,   String> ENTITY_NANO = new HashMap<>();
    private final HashMap<Entity,   Double> ENTITY_HEAT = new HashMap<>();
    private final ArrayList<SPacketScanResult.EntityData> data;
    public AtomicBoolean isShowing = new AtomicBoolean(false);

    private double flash = 1;
    private boolean flashIncreasing = false;

    private final HashMap<ChunkPos, Double> CHUNKS = new HashMap<>();

    public LagOverlayGui(){
        this(new ArrayList<>());
    }

    public LagOverlayGui(ArrayList<SPacketScanResult.EntityData> data){

        MINECRAFT = Minecraft.getMinecraft();
        RENDER_MANAGER = MINECRAFT.getRenderManager();
        FONT_RENDERER = MINECRAFT.fontRendererObj;

        this.data = data;
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

    private void scanAndAddEntities(){
        BLOCKS_NANO.clear();
        BLOCKS_HEAT.clear();
        ENTITY_NANO.clear();
        ENTITY_HEAT.clear();
        CHUNKS.clear();
        if(MINECRAFT.isGamePaused() || MINECRAFT.theWorld == null || MINECRAFT.theWorld.loadedEntityList == null){
            isShowing.set(false);
            return;
        }
        for(SPacketScanResult.EntityData entityData : data){
            long nanos = entityData.getValue(SPacketScanResult.EntityData.Entry.NANOS);
            switch (entityData.type){
                case TILE_ENTITY:
                case BLOCK:
                    if((int) entityData.getValue(SPacketScanResult.EntityData.Entry.WORLD_ID) != MINECRAFT.theWorld.provider.getDimension()){
                        continue;
                    }
                    BlockPos pos = new BlockPos(entityData.getValue(SPacketScanResult.EntityData.Entry.BLOCK_POS_X), entityData.getValue(SPacketScanResult.EntityData.Entry.BLOCK_POS_Y), entityData.getValue(SPacketScanResult.EntityData.Entry.BLOCK_POS_Z));
                    double heat = Calculations.heat(nanos);
                    BLOCKS_HEAT.put(pos,heat);
                    BLOCKS_NANO.put(pos,Calculations.muPerTickString(nanos));
                    calculateChunk(pos,heat);
                    break;
                case ENTITY:
                    if((int) entityData.getValue(SPacketScanResult.EntityData.Entry.WORLD_ID) != MINECRAFT.theWorld.provider.getDimension()){
                        continue;
                    }
                    UUID entityID = entityData.getValue(SPacketScanResult.EntityData.Entry.ENTITY_UUID);
                    for(Entity entity : MINECRAFT.theWorld.loadedEntityList){
                        if(entity.getPersistentID().equals(entityID)){
                            if(entity == MINECRAFT.thePlayer){
                                continue;
                            }
                            ENTITY_NANO.put(entity, Calculations.muPerTickString(nanos));
                            ENTITY_HEAT.put(entity, Calculations.heat(nanos));
                            break;
                        }
                    }
                    break;
            }
        }
    }

    private void calculateChunk(BlockPos block, double heat){
        ChunkPos c = new ChunkPos(block);
        if(CHUNKS.containsKey(c) == false){
            CHUNKS.put(c, heat);
        }else{
            CHUNKS.put(c, heat + CHUNKS.get(c));
        }
        if( heat < 0 && CHUNKS.get(c) == 0){
            CHUNKS.remove(c);
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
        double pX = MINECRAFT.thePlayer.prevPosX + (MINECRAFT.thePlayer.posX - MINECRAFT.thePlayer.prevPosX) * partialTicks;
        double pY = MINECRAFT.thePlayer.prevPosY + (MINECRAFT.thePlayer.posY - MINECRAFT.thePlayer.prevPosY) * partialTicks;
        double pZ = MINECRAFT.thePlayer.prevPosZ + (MINECRAFT.thePlayer.posZ - MINECRAFT.thePlayer.prevPosZ) * partialTicks;

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
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
        GL11.glPopAttrib();

    }

    public void highLightMob(Entity entity, double heat, float partialTicks){

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

    public void drawBlocksAndChunks(double pY){
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

    public void drawEntities(float partialTicks){
        //GL11.glLineWidth(5);
        GL11.glBegin(GL11.GL_LINES);
        for(Map.Entry<Entity, Double> e : ENTITY_HEAT.entrySet()) {
            highLightMob(e.getKey(), e.getValue(), partialTicks);
        }
        GL11.glEnd();
    }

    public void drawTileEntityTags(){
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

    public void drawEntityTags(float partialTicks){
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

    public void hide(){
        MinecraftForge.EVENT_BUS.unregister(this);
        isShowing.set(false);
    }

    public void show(){
        isShowing.set(true);
        new Thread(TIMELY_UPDATES).start();
        MinecraftForge.EVENT_BUS.register(this);
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
                            scanAndAddEntities();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
