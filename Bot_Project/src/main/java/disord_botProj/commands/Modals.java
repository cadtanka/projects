package disord_botProj.commands;

import disord_botProj.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.w3c.dom.Text;

import java.util.List;

public class Modals implements ICommand {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        TextInput name = TextInput.create("name-field", "Name", TextInputStyle.SHORT)
                .setRequired(true)
                .setMinLength(1)
                .setMaxLength(100)
                .build();

        TextInput age = TextInput.create("age-field", "Age", TextInputStyle.SHORT)
                .setRequired(false)
                .build();

        TextInput description = TextInput.create("description-field", "Description", TextInputStyle.SHORT)
                .setRequired(true)
                .setMinLength(1)
                .setPlaceholder("Describe Yourself")
                .build();

        Modal modal = Modal.create("person-modal", "Describe Yourself")
                .addActionRows(ActionRow.of(name), ActionRow.of(age), ActionRow.of(description))
                .build();

        event.replyModal(modal).queue();
    }
}
