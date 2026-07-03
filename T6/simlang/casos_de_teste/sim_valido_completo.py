import sims4.commands

@sims4.commands.Command('aplicar_simlang', command_type=sims4.commands.CommandType.Live)
def aplicar_simlang(_connection=None):
    output = sims4.commands.CheatOutput(_connection)
    output('Aplicando mods gerados pela SimLang...')
    sims4.commands.client_cheat('motherlode', _connection)

    # Configurando o Sim: Maria Eduarda
    sims4.commands.client_cheat('traits.equip_trait trait_Genius', _connection)
    sims4.commands.client_cheat('traits.equip_trait trait_Cheerful', _connection)
    sims4.commands.client_cheat('traits.equip_trait trait_Ambitious', _connection)
    sims4.commands.client_cheat('stats.set_skill_level Major_Programming 8', _connection)
    sims4.commands.client_cheat('stats.set_skill_level Major_Logic 7', _connection)

    # Configurando o Sim: Guilherme
    sims4.commands.client_cheat('traits.equip_trait trait_SelfAssured', _connection)
    sims4.commands.client_cheat('traits.equip_trait trait_Focused', _connection)
    sims4.commands.client_cheat('stats.set_skill_level Major_ResearchDebate 10', _connection)
    sims4.commands.client_cheat('stats.set_skill_level Major_Charisma 5', _connection)
    sims4.commands.client_cheat('stats.set_skill_level Major_Logic 9', _connection)

    output('Todos os atributos foram aplicados com sucesso!')
