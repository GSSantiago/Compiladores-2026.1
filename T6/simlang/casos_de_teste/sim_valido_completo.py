import sims4.commands

@sims4.commands.Command('simlang_ping', command_type=sims4.commands.CommandType.Live, console_type=sims4.commands.CommandType.Cheat)
def simlang_ping(_connection=None):
    output = sims4.commands.CheatOutput(_connection)
    output('SimLang carregado com sucesso.')

@sims4.commands.Command('aplicar_simlang', 'simlang', command_type=sims4.commands.CommandType.Live, console_type=sims4.commands.CommandType.Cheat)
def aplicar_simlang(_connection=None):
    output = sims4.commands.CheatOutput(_connection)
    def executar_comando(comando):
        sims4.commands.client_cheat('|' + comando, _connection)

    output('Aplicando mods gerados pela SimLang...')
    try:
        executar_comando('testingcheats on')
        executar_comando('motherlode')

        # Configurando o Sim: Maria Eduarda
        executar_comando('traits.equip_trait Genius')
        executar_comando('traits.equip_trait Cheerful')
        executar_comando('traits.equip_trait Ambitious')
        executar_comando('stats.set_skill_level Major_Programming 8')
        executar_comando('stats.set_skill_level Major_Logic 7')

        # Configurando o Sim: Guilherme
        executar_comando('traits.equip_trait SelfAssured')
        executar_comando('traits.equip_trait Focused')
        executar_comando('stats.set_skill_level Major_ResearchDebate 10')
        executar_comando('stats.set_skill_level Major_Charisma 5')
        executar_comando('stats.set_skill_level Major_Logic 9')

        output('Todos os atributos foram aplicados com sucesso!')
    except Exception as exc:
        output('Erro ao aplicar SimLang: {}'.format(exc))