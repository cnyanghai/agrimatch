import type { DirectiveBinding } from 'vue'

const observerMap = new WeakMap<Element, IntersectionObserver>()

const createObserver = (el: Element, binding: DirectiveBinding) => {
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          const target = entry.target as HTMLImageElement
          const src = binding.value || binding.arg

          if (src) {
            target.src = src
            target.classList.remove('lazy-loading')
            target.classList.add('lazy-loaded')
          }

          observer.unobserve(target)
          observerMap.delete(target)
        }
      })
    },
    {
      rootMargin: '50px 0px',
      threshold: 0.01
    }
  )

  observerMap.set(el, observer)
  observer.observe(el)
}

export const vLazy = {
  mounted(el: HTMLImageElement, binding: DirectiveBinding) {
    if ('loading' in HTMLImageElement.prototype) {
      el.src = binding.value || binding.arg
      el.classList.remove('lazy-loading')
      el.classList.add('lazy-loaded')
    } else {
      el.classList.add('lazy-loading')
      el.dataset.src = binding.value || binding.arg
      createObserver(el, binding)
    }
  },

  updated(el: HTMLImageElement, binding: DirectiveBinding) {
    if (binding.value !== binding.oldValue) {
      const observer = observerMap.get(el)
      if (observer) {
        observer.unobserve(el)
      }

      el.src = ''
      el.dataset.src = binding.value || binding.arg

      if ('loading' in HTMLImageElement.prototype) {
        el.src = binding.value || binding.arg
      } else {
        createObserver(el, binding)
      }
    }
  },

  unmounted(el: Element) {
    const observer = observerMap.get(el)
    if (observer) {
      observer.disconnect()
      observerMap.delete(el)
    }
  }
}

export default vLazy
