document.addEventListener('DOMContentLoaded', () => {
  // formulários com data-confirma pedem confirmação antes de enviar
  document.querySelectorAll('form[data-confirma]').forEach((form) => {
    form.addEventListener('submit', (e) => {
      if (!window.confirm(form.dataset.confirma)) e.preventDefault()
    })
  })

  // filtros da lista de chamados: aplica ao trocar uma opção
  document.querySelectorAll('form.filtros select').forEach((select) => {
    select.addEventListener('change', () => select.form.submit())
  })
})
