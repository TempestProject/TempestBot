import json
import os

import requests
from localizations import languages, translate_text

application_commands = json.load(open("commands.json", "r"))

application_id = os.environ["APPLICATION_ID"]
bot_token = os.environ["BOT_TOKEN"]

for command in application_commands:
    if command["type"] == 1:
        command["name"] = command["name"].replace(" ", "_")

    name_localizations = {}
    description_localizations = {}
    for language in languages:
        if command["type"] == 1:
            name_localizations.update(
                {
                    language["discord"]: (
                        translate_text(command["name"], language["aws"])
                        .replace(" ", "_")
                        .replace("...", "")
                        .lower()
                    )
                }
            )
        else:
            name_localizations.update(
                {
                    language["discord"]: translate_text(
                        command["name"], language["aws"]
                    ).replace("...", "")
                }
            )
        if "description" in command:
            description_localizations.update(
                {
                    language["discord"]: translate_text(
                        command["description"], language["aws"]
                    )
                }
            )
    command["name_localizations"] = name_localizations

    if "description" in command:
        command["description_localizations"] = description_localizations

    if "options" in command:
        for sub_command_or_group in command["options"]:
            name_localizations = {}
            description_localizations = {}

            for language in languages:
                name_localizations.update(
                    {
                        language["discord"]: (
                            translate_text(
                                sub_command_or_group["name"], language["aws"]
                            )
                            .replace(" ", "_")
                            .replace("...", "")
                            .replace("'", "")
                            .lower()
                        )
                    }
                )
                if "description" in sub_command_or_group:
                    description_localizations.update(
                        {
                            language["discord"]: translate_text(
                                sub_command_or_group["description"], language["aws"]
                            )
                        }
                    )
            sub_command_or_group["name_localizations"] = name_localizations
            sub_command_or_group["name"] = sub_command_or_group["name"].replace(
                " ", "_"
            )

            if "description" in sub_command_or_group:
                sub_command_or_group["description_localizations"] = (
                    description_localizations
                )

            if "options" in sub_command_or_group:
                for sub_command in sub_command_or_group["options"]:
                    name_localizations = {}
                    description_localizations = {}

                    for language in languages:
                        name_localizations.update(
                            {
                                language["discord"]: (
                                    translate_text(sub_command["name"], language["aws"])
                                    .replace(" ", "_")
                                    .replace("...", "")
                                    .lower()
                                )
                            }
                        )
                        if "description" in sub_command:
                            description_localizations.update(
                                {
                                    language["discord"]: translate_text(
                                        sub_command["description"], language["aws"]
                                    )
                                }
                            )
                    sub_command["name_localizations"] = name_localizations
                    sub_command["name"] = sub_command["name"].replace(" ", "_")

                    if "description" in sub_command:
                        sub_command["description_localizations"] = (
                            description_localizations
                        )

# json.dump(application_commands, open("discord.json", "w"), indent=4)
print(
    json.dumps(
        json.loads(
            bytes.decode(
                requests.put(
                    f"https://discord.com/api/v10/applications/{application_id}/commands",
                    headers={
                        "Authorization": f"Bot {bot_token}",
                        "Content-Type": "application/json",
                    },
                    data=json.dumps(application_commands),
                ).content
            )
        ),
        indent=4,
    )
)
