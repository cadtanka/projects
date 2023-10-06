package disord_botProj.commands;

import disord_botProj.ICommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class Mute implements ICommand {
    @Override
    public String getName() {
        return "mute";
    }

    @Override
    public String getDescription() {
        return "Will mute a member";
    }

    @Override
    public List<OptionData> getOptions() {
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.USER, "muted", "The user to mute", true));
        return options;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();
        Role role = guild.getRoleById(1151321264607203349L);

        if(member.getRoles().contains(role)) {
            Member mutedMember = event.getOption("muted").getAsMember();
            Role muteRole = guild.getRoleById(1151379653026648186L);
            Role defaultRole = guild.getRoleById(1151327548626710550L);
            guild.removeRoleFromMember(mutedMember, defaultRole).queue();
            guild.addRoleToMember(mutedMember, muteRole).queue();
            event.reply("Muted Member");

        } else {
            event.reply("You do not have the required privileges").queue();
        }
    }
}
