package cf.terminator.laggoggles.command;

import cf.terminator.laggoggles.Main;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class LagGogglesCommand extends CommandBase {


    @Override
    public String getName() {
        return Main.MODID;
    }


    @Override
    public String getUsage(ICommandSender sender) {
        return null;
    }

    @Override
    public List<String> getAliases(){
        ArrayList<String> list = new ArrayList<>();
        list.add("lagcheck");
        list.add("lagscan");
        list.add("lag");
        return list;
    }


    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

    }
}
