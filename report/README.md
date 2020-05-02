# Relatório do projeto Sauron

Sistemas Distribuídos 2019-2020, segundo semestre


## Autores

**Grupo A41**

| Número | Nome                   | Utilizador                              | Correio Eletrónico                               |
| -------|------------------------|-----------------------------------| ------------------------------------|
| 89425  | Daniel Pereira         | <https://github.com/DanielPereira890>     | <mailto:daniel.r.pereira@tecnico.ulisboa.pt>     |
| 89445  | Francisco Serralheiro  | <https://github.com/Serralheiro> | <mailto:francisco.serralheiro@tecnico.ulisboa.pt> |
| 89549  | Tiago Barroso          | <https://github.com/TiagoMAB>     | <mailto:tiago.agostinho.barroso@tecnico.ulisboa.pt>   |

![Daniel Pereira](daniel.png)
![Francisco Serralheiro](francisco.jpg)
![Tiago Barroso](tiago.jpg)  

## Melhorias da primeira parte

- [No cam_join é agora permitido registar uma câmera com nome igual a uma existente, desde que as coordenadas sejam idênticas.](https://github.com/tecnico-distsys/A41-Sauron/commit/f7c328e8403595ee158c5a11cfde6e4f5f78a99f)


## Modelo de faltas

 #### Base:
 
* O sistema é assíncrono e a comunicação pode omitir mensagens;
* Os gestores de réplica podem falhar silenciosamente mas não arbitrariamente;
* Embora o conjunto de gestores de réplica seja estático, os seus endereços não são conhecidos a priori e podem variar ao longo do tempo;
* Existe sempre, pelo menos, uma réplica ativa para atender os clientes;
* As falhas das réplicas são transientes e não definitivas.

----

#### Especificas:

* Garante-se que dada uma leitura(`l1`) que reflete `u1`, se forem feitas duas leituras seguintes (`l2` e `l3`) que refletem, respetivamente e apenas, `u2` e `u3`, mas não `u1`, o cliente recebe uma resposta que:
    * Descarta `l1` se e só se `l2`/`l3`, no caso de um spot, refletir uma observação com o mesmo id e a mesma câmera, mas timestamp mais recente;
    * Descarta `l2`/`l3` se e só se `l1`, no caso de um spot, refletir uma observação com o mesmo id e a mesma câmera, mas timestamp mais recente;
    * Combina `l1` e `l2` ou `l1` e `l3`, no caso de um trail, de modo a apresentar uma resposta mais coerente possível;
    * Não combina `l2` e `l3`, no caso de um trail, pois `l2` e `l3` são ambas leituras desatualizadas (ou seja, não refletem pelo menos 1 update recebido anteriormente pelo cliente) e, portanto, não resultam em atualizações da cache.

----

## Solução

![Solution](solution.bmp)  

A solução implementada é baseada no uso de uma cache e timestamp vetorial local para obter uma resposta fracamente coerente em queries do spotter. 

Ao ser feita uma query a uma réplica, é verificado se o timestamp dado como resposta é mais recente que o guardado localmente, adicionando as observações à cache caso seja verificado. 

No caso de o timestamp dado como resposta refletir um estado mais antigo ao estado de uma réplica que foi contactada anteriormente, o frontend tenta formar uma resposta mais coerente, usando a informação obtida da query e consultando com a informação na cache, adicionando observações da cache à resposta. Esta situação pode-se verificar no caso em que uma réplica anterior tenha sofrido uma falha silenciosa e parado a sua execução antes de ter mandado a sua mensagem gossip periódica às outras réplicas.

No exemplo da figura, os servidores 1 e 3 estão disponíveis. O eye faz um report das observações 1, 2 e 3 com o mesmo id para o servidor 1, que vão ser enviadas por mensagem gossip ao servidor 3. Entretanto um spotter conecta-se ao servidor 1 e executa o comando trail, obtendo as 3 observações registadas pelo eye. No entanto, o servidor 1 falha e liga-se o servidor 2, ao qual um novo eye submete uma observação 4 do objecto com o id do report anterior. Se o spotter agora fizer um novo trail, o frontend vai reconectar-se a outro servidor. Se esse for o servidor 2, o trail vai devolver só a observação 4. O frontend nesta situação vai verificar que a réplica está desatualizada e construir uma resposta que contém as observações 1, 2, 3 (já guardadas na cache no trail anterior) e 4, devolvendo-a ao spotter.

----

## Protocolo de replicação

Para as réplicas comunicarem entre si, é utilizado um temporizador que a cada 30 segundos comunica com todas as réplicas disponíveis. Cada réplica mantém um vetor com o seu timestamp vetorial e com os timestamps vetoriais das outras réplicas. O timestamp vetorial de uma dada réplica é atualizado quando é lido um update (camJoin ou report);

O timestamp vetorial das restantes réplicas é atualizado após ser recebida a resposta à mensagem gossip enviada.

Uma réplica envia os updates que prevê que cada outra réplica conhecida ainda não possui.

Na receção de uma mensagem gossip, a réplica vai verificar quais dos updates que lhe foram enviados já estão guardados no seu update log. Durante este processo, o timestamp vetorial também é atualizado também com o timestamp vetorial que lhe foi enviado. No final deste processo, a réplica envia de volta, como resposta à mensagem gossip, o seu timestamp vetorial.

Os updates são guardados num update log, que é atualizado sempre que um cliente manda informação para o servidor ou quando uma réplica envia informação para outra réplica. Visto que não há causalidade entre operações, esta não foi uma consideração que foi tida em conta na implementação do código.


---- 

## Opções de implementação

### Servidor

* Cada update é uma classe que contém:
    * no caso do cam_join(), apenas uma câmera (objeto de domínio); 
    * no caso do report(), uma câmera e uma lista de observacões (objetos de dominio);
* Um update_log é uma classe que contém: 
    * uma lista de objetos update;
    * um número de instância - correspondente à réplica respetiva;
    * uma lista de timestamps vetoriais;
* A lista de timestamps vetoriais é um vetor de vetores de inteiros, em que cada sub-vetor de inteiros corresponde ao timestamp vetorial de cada réplica. Cada número inteiro armazenado, denomina-se número de sequência, representa o instante temporal relativo em que essa réplica se encontra.
* Por exemplo, o número de sequência na posição (1,2) representa o instante temporal da réplica 2 relativamente à réplica 1.
* Não regista updates que já tenham sido recebidos, fazendo uma comparação entre o número de sequência de cada update recebido numa resposta gossip e o que está guardado no seu timestamp vetorial local. Só regista no update log aquelas com número de sequência maior pois estas são as que a réplica não tem registadas.
* Uma réplica não envia updates que sabe que a réplica de destino já tenha recebido anteriormente, fazendo isto através de uma verificação à lista local dos timestamps vetoriais e escolhendo enviar apenas updates com número de sequência maior ao último número de sequência que lhe foi comunicado numa resposta ao gossip.

---- 

### Cliente

* No frontend é mantida uma cache de respostas e um timestamp vetorial.
* A cache é uma classe que contém:
    * uma constante limit - configurável na sua criação, corresponde ao limite da cache:
    * uma lista de observações spots - guarda as últimas observações recebidas por cada comando spot do cliente;
    * uma lista de observações trails - guarda as últimas observações recebidas por cada comando trail do cliente;
* A cache de respostas e o timestamp vetorial só são atualizados quando é recebida uma resposta de um servidor com estado que reflete todos os updates já contidos numa resposta anterior.
* Se uma réplica falhar, o erro de comunicação é corrigido no frontend, escolhendo aleatoriamente uma das réplicas disponíveis para conectar e guardando o número de instância dessa réplica (se não for escolhido o número de instância) ou tentando reconectar-se um número de fixo de vezes à mesma réplica (se for escolhido o número de instância).
