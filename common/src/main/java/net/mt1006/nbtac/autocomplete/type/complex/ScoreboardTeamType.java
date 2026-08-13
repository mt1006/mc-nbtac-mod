package net.mt1006.nbtac.autocomplete.type.complex;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.scores.PlayerTeam;
import net.mt1006.nbtac.autocomplete.SuggestionList;
import net.mt1006.nbtac.autocomplete.suggestions.StringSuggestion;
import net.mt1006.nbtac.autocomplete.type.PrimitiveType;

public class ScoreboardTeamType extends ComplexType
{
	public static final ScoreboardTeamType INSTANCE = new ScoreboardTeamType();

	public ScoreboardTeamType()
	{
		super(PrimitiveType.STRING);
	}

	@Override public void getBasicSuggestions(SuggestionListContext ctx, SuggestionList list)
	{
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection == null) { return; }

		for (PlayerTeam team : connection.scoreboard().getPlayerTeams())
		{
			list.add(new StringSuggestion(team.getName(), "[#scoreboard_team]", ctx.parserType()));
		}
	}
}
