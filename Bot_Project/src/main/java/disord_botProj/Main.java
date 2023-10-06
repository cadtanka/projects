package disord_botProj;

import disord_botProj.commands.*;
import disord_botProj.commands.Play;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class Main {

    public static void main(String[] args) throws LoginException {
        JDA jda = JDABuilder.createDefault("MTE1MDQ5Mjg2MjU1MTY5OTU2Ng.GozAJY.SnIVK9fV0Izfxo_JCq9GnDwg_y6uX3KHKATTN0").build();
        jda.addEventListener(new Listeners());

        CommandManager manager = new CommandManager();
        manager.add(new Sum());
        manager.add(new Subtract());
        manager.add(new Embed());
        manager.add(new Buttons());
        manager.add(new Modals());
        manager.add(new Staff());
        manager.add(new Ban());
        manager.add(new UnStaff());
        manager.add(new Mute());
        manager.add(new Unmute());
        manager.add(new Play());
        jda.addEventListener(manager);


    }
}
