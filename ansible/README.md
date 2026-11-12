# Ansible deployment

Deploys the Lost and Found jar as a systemd service (`lost-found`, user `lostfound`, port 8090) using `ansible_connection=local`.

## Prerequisites
- Build the jar first: `./mvnw clean package` (produces `target/lost-found.jar`).
- The target must be Ubuntu/Debian with systemd and passwordless sudo (the role uses apt). On macOS use an Ubuntu VM or container host.

## Run
```
cd ansible
ansible-playbook -i inventory.ini playbook.yml
```

## Idempotency demo
Run the same command a second time; it should report `changed=0`.

## Verify
```
systemctl status lost-found
curl localhost:8090/actuator/health
```

## Layout
`roles/lost_found/` follows the `ansible-galaxy init` layout. Variables are in `roles/lost_found/defaults/main.yml`.
