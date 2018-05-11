

# Fillfactor

# --- !Ups

alter table orders set (fillfactor='90');
alter table balances set (fillfactor='70');


# --- !Downs

alter table balances reset (fillfactor);
alter table orders reset (fillfactor);
