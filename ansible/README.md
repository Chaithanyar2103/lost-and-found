# Ansible deployment

Deploys the Lost and Found jar as a systemd service (`lost-found`, user `lostfound`, port 8090) to a Multipass Ubuntu VM over SSH.

## Setup
- Target: Multipass Ubuntu VM (systemd, apt, passwordless sudo for `ubuntu`), reached over SSH.
- Control machine: macOS, with Ansible installed.
- Inventory (`inventory.ini`): host `lostfound-vm` in group `lost_found_servers`, `ansible_host=192.168.252.2`, `ansible_user=ubuntu`, key `~/.ssh/ansible_node`, interpreter `/usr/bin/python3`.
- The jar is read from `target/lost-found.jar` on the control machine (`jar_src`) and copied to the VM. The jar-exists pre-task runs on the control machine; the health check runs on the VM against `127.0.0.1:8090`.

## Prerequisites
Build the jar on the control machine first:
```
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./mvnw clean package
```

## Run (from the repository root)
```
ansible-playbook -i ansible/inventory.ini ansible/playbook.yml --syntax-check
ansible-playbook -i ansible/inventory.ini ansible/playbook.yml
```

## Idempotency demo
Run the playbook a second time; it should report `changed=0`.

## Verify (on the VM)
```
multipass shell <vm-name>
systemctl status lost-found
curl 127.0.0.1:8090/actuator/health
```

## Status
Only `--syntax-check` has been run. The playbook has not yet been run against the VM.

## Layout
`roles/lost_found/` follows the `ansible-galaxy init` layout. Variables are in `roles/lost_found/defaults/main.yml`.
