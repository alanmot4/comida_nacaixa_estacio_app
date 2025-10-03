import { useEffect, useState } from "react";
import { supabase } from "@/integrations/supabase/client";
import { MarmitaCard } from "@/components/MarmitaCard";
import { Cart } from "@/components/Cart";
import { CheckoutDialog } from "@/components/CheckoutDialog";
import { useCart } from "@/hooks/useCart";
import { Button } from "@/components/ui/button";
import { ShoppingCart, Phone, MapPin, Clock } from "lucide-react";
import heroImage from "@/assets/hero-marmitas.jpg";
import tradicionalImg from "@/assets/marmita-tradicional.jpg";
import fitnessImg from "@/assets/marmita-fitness.jpg";
import vegetarianaImg from "@/assets/marmita-vegetariana.jpg";
import executivaImg from "@/assets/marmita-executiva.jpg";

interface Marmita {
  id: string;
  name: string;
  description: string;
  price: number;
  image_url?: string;
  category_id: string;
}

const imageMap: Record<string, string> = {
  "Marmita Tradicional": tradicionalImg,
  "Marmita Fitness": fitnessImg,
  "Marmita Vegetariana": vegetarianaImg,
  "Marmita Executiva": executivaImg,
};

const Index = () => {
  const [marmitas, setMarmitas] = useState<Marmita[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCheckout, setShowCheckout] = useState(false);
  const cart = useCart();

  useEffect(() => {
    loadMarmitas();
  }, []);

  const loadMarmitas = async () => {
    try {
      const { data, error } = await supabase
        .from("marmitas")
        .select("*")
        .eq("available", true)
        .order("name");

      if (error) throw error;
      setMarmitas(data || []);
    } catch (error) {
      console.error("Error loading marmitas:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = (marmita: Marmita) => {
    cart.addItem({
      id: marmita.id,
      name: marmita.name,
      price: marmita.price,
      image_url: imageMap[marmita.name],
    });
  };

  const handleCheckoutSuccess = () => {
    cart.clearCart();
  };

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="sticky top-0 z-50 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 border-b">
        <div className="container mx-auto px-4 h-16 flex items-center justify-between">
          <h1 className="text-2xl font-bold text-primary">Sabor & Praticidade</h1>
          <Button
            variant="outline"
            size="default"
            className="relative"
            onClick={() => cart.setIsOpen(true)}
          >
            <ShoppingCart className="w-5 h-5" />
            {cart.itemCount > 0 && (
              <span className="absolute -top-2 -right-2 bg-primary text-primary-foreground w-6 h-6 rounded-full text-xs flex items-center justify-center font-bold">
                {cart.itemCount}
              </span>
            )}
          </Button>
        </div>
      </header>

      {/* Hero Section */}
      <section className="relative h-[500px] overflow-hidden">
        <div
          className="absolute inset-0 bg-cover bg-center"
          style={{
            backgroundImage: `linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url(${heroImage})`,
          }}
        />
        <div className="relative container mx-auto px-4 h-full flex items-center">
          <div className="max-w-2xl text-white">
            <h2 className="text-5xl font-bold mb-4">
              Marmitas Deliciosas
              <br />
              Direto na Sua Casa
            </h2>
            <p className="text-xl mb-8 text-white/90">
              Refeições caseiras, frescas e saborosas preparadas com carinho
            </p>
            <Button
              variant="hero"
              size="lg"
              onClick={() => {
                document
                  .getElementById("cardapio")
                  ?.scrollIntoView({ behavior: "smooth" });
              }}
            >
              Ver Cardápio
            </Button>
          </div>
        </div>
      </section>

      {/* Info Cards */}
      <section className="py-12 bg-muted/30">
        <div className="container mx-auto px-4">
          <div className="grid md:grid-cols-3 gap-6">
            <div className="flex items-center gap-4 p-6 bg-card rounded-lg shadow-[var(--shadow-card)]">
              <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center">
                <Clock className="w-6 h-6 text-primary" />
              </div>
              <div>
                <h3 className="font-bold">Horário</h3>
                <p className="text-sm text-muted-foreground">Seg-Sex: 11h-14h</p>
              </div>
            </div>
            <div className="flex items-center gap-4 p-6 bg-card rounded-lg shadow-[var(--shadow-card)]">
              <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center">
                <MapPin className="w-6 h-6 text-primary" />
              </div>
              <div>
                <h3 className="font-bold">Entrega</h3>
                <p className="text-sm text-muted-foreground">Região central</p>
              </div>
            </div>
            <div className="flex items-center gap-4 p-6 bg-card rounded-lg shadow-[var(--shadow-card)]">
              <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center">
                <Phone className="w-6 h-6 text-primary" />
              </div>
              <div>
                <h3 className="font-bold">Contato</h3>
                <p className="text-sm text-muted-foreground">(11) 99999-9999</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Menu Section */}
      <section id="cardapio" className="py-16">
        <div className="container mx-auto px-4">
          <div className="text-center mb-12">
            <h2 className="text-4xl font-bold mb-4">Nosso Cardápio</h2>
            <p className="text-xl text-muted-foreground">
              Escolha sua marmita favorita
            </p>
          </div>

          {loading ? (
            <div className="text-center py-12">
              <p className="text-muted-foreground">Carregando cardápio...</p>
            </div>
          ) : (
            <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
              {marmitas.map((marmita) => (
                <MarmitaCard
                  key={marmita.id}
                  id={marmita.id}
                  name={marmita.name}
                  description={marmita.description || ""}
                  price={marmita.price}
                  image_url={imageMap[marmita.name]}
                  onAddToCart={() => handleAddToCart(marmita)}
                />
              ))}
            </div>
          )}
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-muted py-8">
        <div className="container mx-auto px-4 text-center text-muted-foreground">
          <p>© 2025 Sabor & Praticidade - Todos os direitos reservados</p>
        </div>
      </footer>

      {/* Cart */}
      <Cart
        isOpen={cart.isOpen}
        onClose={() => cart.setIsOpen(false)}
        items={cart.items}
        total={cart.total}
        onUpdateQuantity={cart.updateQuantity}
        onRemoveItem={cart.removeItem}
        onCheckout={() => {
          cart.setIsOpen(false);
          setShowCheckout(true);
        }}
      />

      {/* Checkout Dialog */}
      <CheckoutDialog
        isOpen={showCheckout}
        onClose={() => setShowCheckout(false)}
        items={cart.items}
        total={cart.total}
        onSuccess={handleCheckoutSuccess}
      />
    </div>
  );
};

export default Index;
