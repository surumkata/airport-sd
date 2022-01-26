## Trabalho prático da UC de Sistemas Distribuídos - Ano letivo de 2021/2022

<p align="center">
  <img src="https://user-images.githubusercontent.com/61991247/109984429-ac0b7680-7cfb-11eb-9f4f-f5bf5b3e4b7b.png">
</p>

### Funcionalidades
```
- Autenticação e registo de utilizador, dado o seu nome e palavra-passe. Sempre que um utilizador desejar interagir com o serviço deverá estabelecer uma conexão e ser autenticado pelo servidor
- Autenticação de um utilizador especial de administração (admin)
- Inserção de informação sobre voos (origem, destino, capacidade) pelo administrador
- Encerramento de um dia por parte do administrador, impedindo novas reservas para esse dia e cancelamento de reservas desse dia
- Reserva de uma viagem indicando o percurso completo com todas as escalas (por exemplo Porto → London → Tokyo, ou seja, de Porto a Tokyo com uma escala em London) e um intervalo de datas possíveis, deixando ao serviço a escolha de uma data em que a viagem seja possível. O servidor deverá responder com o código de reserva
- Cancelamento da reserva de uma viagem, usando o código de reserva, a pedido do utilizador a que pertence
- Obtenção da lista de todas os voos existentes (lista de pares origem→destino), a pedido do utilizador
```