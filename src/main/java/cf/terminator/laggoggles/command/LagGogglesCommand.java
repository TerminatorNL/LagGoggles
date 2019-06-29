package cf.terminator.laggoggles.command;

import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.api.Profiler;
import cf.terminator.laggoggles.client.gui.GuiScanResultsWorld;
import cf.terminator.laggoggles.packet.ObjectData;
import cf.terminator.laggoggles.profiler.ProfileResult;
import cf.terminator.laggoggles.profiler.ScanType;
import cf.terminator.laggoggles.server.RequestResultHandler;
import cf.terminator.laggoggles.server.ScanRequestHandler;
import cf.terminator.laggoggles.util.Perms;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import static cf.terminator.laggoggles.util.ClickableLink.getLink;

public class LagGogglesCommand extends CommandBase {

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
        return true;
    }

    @Override
    public String getName() {
        return Main.MODID.toLowerCase();
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/" + getName();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(args.length == 2 && args[0].equalsIgnoreCase("start")){
            if(hasPerms(sender, Perms.Permission.START) == false){
                throw new CommandException("You don't have permission to do this!");
            }
            final int seconds = parseInt(args[1]);
            if(Profiler.canProfile() == false){
                throw new CommandException("Profiler is already running.");
            }
            if(sender instanceof EntityPlayerMP && hasPerms(sender, Perms.Permission.FULL) == false){
                long secondsLeft = ScanRequestHandler.secondsLeft(((EntityPlayerMP) sender).getGameProfile().getId());
                if(secondsLeft > 0) {
                    throw new CommandException("Please wait " + secondsLeft + " seconds.");
                }
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Profiler.runProfiler(seconds, ScanType.WORLD, sender);
                    sender.sendMessage(new TextComponentString(TextFormatting.GRAY + Main.MODID + TextFormatting.WHITE + ": You can see results using /" + getName() +" dump"));
                }
            }).start();
            return;
        }
        if(args.length == 1 && args[0].equalsIgnoreCase("dump")){
            if(hasPerms(sender, Perms.Permission.GET) == false){
                throw new CommandException("You don't have permission to do this!");
            }
            dump(sender);
            return;
        }
        sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "Running LagGoggles version: " + TextFormatting.GREEN + Main.VERSION));
        sender.sendMessage(getLink("https://minecraft.curseforge.com/projects/laggoggles"));
        sender.sendMessage(new TextComponentString(""));
        sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "Available arguments:"));
        sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "/" + getName() + " " +TextFormatting.WHITE + "start <seconds>"));
        sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "/" + getName() + " " +TextFormatting.WHITE + "dump"));
    }

    private boolean hasPerms(ICommandSender sender, Perms.Permission permission){
        if(sender instanceof MinecraftServer){
            return true;
        }else if(sender instanceof EntityPlayerMP){
            return Perms.hasPermission(((EntityPlayerMP) sender), permission);
        }else{
            Main.LOGGER.info("Unknown object is executing a command, assuming it's okay. Object: (" + sender + ") Class: (" + sender.getClass().toString() + ")");
            return true;
        }
    }

    private void dump(ICommandSender sender) throws CommandException{
        ProfileResult fullResult = Profiler.getLatestResult();
        if(fullResult == null){
            throw new CommandException("No result available.");
        }
        if(fullResult.getType() != ScanType.WORLD){
            throw new CommandException("Result is not of type WORLD.");
        }
        ProfileResult result;
        if(sender instanceof EntityPlayerMP && hasPerms(sender, Perms.Permission.FULL) == false){
            long secondsLeft = RequestResultHandler.secondsLeft(((EntityPlayerMP) sender).getGameProfile().getId());
            if(secondsLeft > 0){
                throw new CommandException("Please wait " + secondsLeft + " seconds.");
            }
            result = Perms.getResultFor(((EntityPlayerMP) sender), fullResult);
        }else{
            result = fullResult;
        }
        msg(sender, "Total ticks", result.getTickCount());
        msg(sender, "Total time", result.getTotalTime()/1000/1000/1000 + " seconds");
        msg(sender, "TPS", Math.round(result.getTPS() * 100D)/100D);
        title(sender, "ENTITIES");
        boolean has = false;
        for(GuiScanResultsWorld.LagSource source : result.getLagSources()){
            if(source.data.type == ObjectData.Type.ENTITY) {
                msg(sender, muPerTickString(source.nanos, result), source.data);
                has = true;
            }
        }
        if(has == false){
            sender.sendMessage(new TextComponentString("None"));
        }
        has = false;
        title(sender, "TILE ENTITIES");
        for(GuiScanResultsWorld.LagSource source : result.getLagSources()){
            if(source.data.type == ObjectData.Type.TILE_ENTITY) {
                msg(sender, muPerTickString(source.nanos, result), source.data);
                has = true;
            }
        }
        if(has == false){
            sender.sendMessage(new TextComponentString("None"));
        }
        has = false;
        title(sender, "BLOCKS");
        for(GuiScanResultsWorld.LagSource source : result.getLagSources()){
            if(source.data.type == ObjectData.Type.BLOCK) {
                msg(sender, muPerTickString(source.nanos, result), source.data);
                has = true;
            }
        }
        if(has == false){
            sender.sendMessage(new TextComponentString("None"));
        }
        has = false;
        title(sender, "EVENTS");
        for(GuiScanResultsWorld.LagSource source : result.getLagSources()){
            if(source.data.type == ObjectData.Type.EVENT_BUS_LISTENER) {
                msg(sender, muPerTickString(source.nanos, result), source.data);
                has = true;
            }
        }
        if(has == false){
            sender.sendMessage(new TextComponentString("None"));
        }
        title(sender, "END");
        sender.sendMessage(new TextComponentString("Results printed, copy your log."));
    }

    private void msg(ICommandSender sender, String key, Object value){
        sender.sendMessage(new TextComponentString(key + ": " + value));
    }

    private void title(ICommandSender sender, String title){
        sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "---[ " + title + " ]---"));
    }

    private static String muPerTickString(long nanos, ProfileResult result) {
        if(result == null){
            return "?";
        }
        return Double.valueOf((nanos / result.getTickCount()) / 1000).intValue() + " micro-s/t";
    }

}
